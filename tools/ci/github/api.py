# Copyright (c) 2023 Marcin Zdun
# This code is licensed under MIT license (see LICENSE for details)

import json
import sys
from pprint import pprint
from typing import Any, List, Optional, Union

from ..common.changelog import ChangeLog, GithubReleaseChangelog
from ..common.project import Project
from ..common.runner import (Environment, capture, checked, checked_capture,
                             print_args)


def locate_remote(owner: str, repo: str) -> Optional[str]:
    proc = capture("git", "remote", "-v")

    expected = [
        f"https://github.com/{owner}/{repo}.git",
        f"git@github.com:{owner}/{repo}.git",
    ]
    for remote_name, url in [
        line[:-7].split("\t")
        for line in proc.stdout.decode("UTF-8").split("\n")
        if line[-7:] == " (push)"
    ]:
        if url in expected:
            if Environment.DBG:
                print(f"[DEBUG] {remote_name=}", file=sys.stderr)
                print(f"[DEBUG] {url=}", file=sys.stderr)
            return remote_name

    return None


def format_release(log: ChangeLog, cur_tag: str, prev_tag: str, github_link: str):
    body = GithubReleaseChangelog(github_link, cur_tag, prev_tag).format_changelog(log)

    return {
        "tag_name": cur_tag,
        "name": cur_tag,
        "body": body,
        "draft": True,
        "prerelease": len(cur_tag.split("-", 1)) > 1,
    }


def gh(
    url: str,
    *args: str,
    method: Optional[str] = None,
    capture_output: bool = True,
    **kwargs,
):
    accept = kwargs.get("accept", "application/vnd.github+json")
    cmd = [
        "gh",
        "api",
        "-H",
        f"Accept: {accept}",
        "-H",
        "X-GitHub-Api-Version: 2022-11-28",
    ]
    if method:
        cmd.extend(["--method", method.upper()])
    if Environment.DRY_RUN:
        print_args((*cmd, url, *args))
        return None
    return (
        checked_capture(*cmd, url, *args)
        if capture_output
        else checked(*cmd, url, *args)
    )


class API:
    owner: str
    repo: str
    remote: Optional[str]

    @property
    def url(self):
        return f"https://github.com/{self.owner}/{self.repo}"

    @property
    def root(self):
        return f"/repos/{self.owner}/{self.repo}"

    def __init__(self, owner: str, repo: str):
        self.owner = owner
        self.repo = repo
        self.remote = locate_remote(owner, repo)

    @classmethod
    def fromProject(finalClass, project: Project):
        if project.github is None:
            return None
        return finalClass(project.github.owner, project.github.repo)

    def gh(
        self,
        res: str,
        *args: str,
        method: Optional[str] = None,
        server: Optional[str] = None,
        capture_output: bool = True,
        **kwargs,
    ):
        return gh(
            f"{self.root}{res}",
            *args,
            method=method,
            server=server,
            capture_output=capture_output,
            **kwargs,
        )

    def json_from(
        self,
        res: str,
        *args: str,
        method: Optional[str] = None,
        server: Optional[str] = None,
        default: Any = {},
    ):
        proc = self.gh(res, *args, method=method, server=server)
        if proc is None:
            return default
        stdout = proc.stdout.decode("UTF-8")
        if Environment.DBG:
            print("[DEBUG] stdout=", file=sys.stderr)
            pprint(json.loads(stdout), stream=sys.stderr)
        return json.loads(stdout)

    def first_from(self, res: str, root_name: None = None, **filter):
        default = [] if root_name is None else {}
        data: Union[dict, list] = self.json_from(res, default=default)
        if root_name is not None:
            data = data.get(root_name, [])

        for item in data:
            sel = True
            for key in filter:
                if item.get(key) != filter[key]:
                    sel = False
                    break
            if sel:
                return item

        return {}

    def release(self, gh_release: dict) -> dict:
        checked(
            "git", "push", self.remote, "main", "--follow-tags", "--force-with-lease"
        )

        flags = []
        for name, value in gh_release.items():
            is_str = isinstance(value, str)
            if not is_str:
                value = json.dumps(value)
            flags.append("-f" if is_str else "-F")
            flags.append(f"{name}={value}")

        return self.json_from("/releases", *flags, method="POST", default={})

    def get_unpublished_release(self, tag_name: str):
        return self.first_from(f"/releases", tag_name=tag_name)

    def upload_assets(self, tag_name: str, paths: List[str]):
        checked("gh", "release", "upload", tag_name, *paths, "--clobber")

    def publish_release(self, release_id: int) -> Optional[str]:
        return self.json_from(
            f"/releases/{release_id}",
            "-f",
            "draft=false",
            "-F",
            "make_latest=legacy",
            method="PATCH",
        ).get("html_url")
