#!/usr/bin/env python
# Copyright (c) 2023 Marcin Zdun
# This code is licensed under MIT license (see LICENSE for details)

import argparse
import hashlib
import io
import os
import re
import sys
import zipfile
import subprocess
from typing import List, Optional

from github.api import API, format_release
from github.changelog import FORCED_LEVEL, LEVEL_STABILITY, LEVEL_BENIGN, update_changelog
from github.gradle import get_version, set_version
from github.git import add_files, annotated_tag, bump_version, commit, get_log, get_tags
from github.runner import Environment, print_args

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
GITHUB_ORG = "mzdun"
SCOPE_FIX = {}

#####################################################################


def release(
    take_all: bool,
    forced_level: Optional[int],
    stability: Optional[str],
    show_changelog: bool,
):
    project = get_version()
    github_link = f"https://github.com/{GITHUB_ORG}/{project.repo.value}"
    tags = get_tags(project)
    log, level = get_log(tags, SCOPE_FIX, take_all)
    force_stability = False

    if forced_level is not None:
        force_stability = forced_level == LEVEL_STABILITY
        level = forced_level if not force_stability else level

    next_stability = project.stability.value
    if stability is not None:
        stability.strip("-")
        if len(stability):
            stability = f"-{stability}"
        next_stability = stability
    if force_stability:
        if next_stability:
            stability_parts = next_stability.split('.')
            if len(stability_parts) == 1:
                next_stability = f"{next_stability}.2"
            else:
                iteration = int(stability_parts[1]) + 1
                stability_parts[1] = str(iteration)
                next_stability = '.'.join(stability_parts)
            level = LEVEL_BENIGN
        else:
            next_stability = "-rc.1"
    else:
        if next_stability[:4] == '-rc.':
            next_stability = ''
            level = LEVEL_BENIGN
    next_tag = f"v{bump_version(project.ver(), level)}{next_stability}"
    additional_changed_files = []
    if not Environment.DRY_RUN or show_changelog:
        update_changelog(log, next_tag, project.tag(), github_link)
        set_version(next_tag[1:])

    commit_message = f"release {next_tag[1:]}"

    add_files(os.path.join(ROOT, "gradle.properties"),
              os.path.join(ROOT, "CHANGELOG.md"),
              *additional_changed_files)
    commit(f"chore: {commit_message}")
    annotated_tag(next_tag, commit_message)

    api = API(GITHUB_ORG, project.repo.value)

    if api.remote is not None:
        gh_release = format_release(log, next_tag, project.tag(), github_link)
        html_url = api.release(gh_release).get("html_url")
        if html_url is not None:
            print(f"Visit draft at {html_url}")


def _hash(filename: str) -> str:
    sha = hashlib.sha256()
    with open(filename, "rb") as data:
        for block in iter(lambda: data.read(io.DEFAULT_BUFFER_SIZE), b""):
            sha.update(block)
    return sha.hexdigest()


def _checksums(archive: str, names: List[str], out_name: str):
    print_args(["sha256sum", "-b"])
    if True or not Environment.DRY_RUN:
        with open(os.path.join(archive, out_name), "w") as output:
            for name in names:
                digest = _hash(os.path.join(archive, name))
                print(f"{digest} *{name}", file=output)
    names.append(out_name)


def upload(archive: str):
    project = get_version()
    api = API(GITHUB_ORG, project.repo.value)
    regex = f"^{project.pkg()}\\+.*\\.jar*$"
    matcher = re.compile(regex)

    if os.path.isdir(archive):
        for _, dirnames, filenames in os.walk(archive):
            dirnames[:] = []
            names = [name for name in filenames if matcher.match(name)]
    else:
        next_archive = f"{archive}-dir"
        os.makedirs(next_archive, exist_ok=True)
        with zipfile.ZipFile(archive) as zip:
            names = [name for name in zip.namelist() if matcher.match(name)]
            for name in names:
                zip.extract(name, path=next_archive)
        archive = next_archive
    if not len(names):
        print(f"no artifact matches {regex}", file=sys.stderr)
        return None

    _checksums(archive, names, "sha256sum.txt")

    release_id = api.get_unpublished_release(project.tag()).get("id")

    if release_id is not None:
        os.chdir(archive)
        api.upload_assets(project.tag(), names)
        html_url = api.publish_release(release_id)
        if html_url is not None:
            print(">>>", html_url, file=sys.stderr)
    elif Environment.DRY_RUN:
        if len(names):
            print(f"would upload:", file=sys.stderr)
        for name in names:
            print(f"- {name}", file=sys.stderr)


parser = argparse.ArgumentParser(
    description="Creates a release draft in GitHub")
parser.add_argument(
    "--dry-run",
    action="store_true",
    required=False,
    help="print commands, change nothing",
)
parser.add_argument(
    "--show-changelog",
    action="store_true",
    required=False,
    help="modifies CHANGELOG.md and gradle.properties even with --dry-run",
)
parser.add_argument(
    "--all",
    required=False,
    action="store_true",
    help="create a changelog with all sections, not only the 'feat', 'fix' and 'breaking'",
)
parser.add_argument(
    "--force",
    required=False,
    help="ignore the version change from changelog and instead use this settings' change",
    choices=FORCED_LEVEL.keys(),
)
parser.add_argument(
    "--stability",
    required=False,
    help="change the stability of the version",
)
parser.add_argument(
    "--upload",
    metavar="ZIP-or-dir",
    required=False,
    help="instead of creating a new release, upload to and publish an existing one",
)
parser.add_argument(
    "--color",
    required=False,
    help="should we colorize the output",
    choices=["always", "never"],
    default="always",
)
parser.add_argument(
    "--debug",
    required=False,
    action="store_true",
)


def __main__():
    args = parser.parse_args()
    Environment.DRY_RUN = args.dry_run
    Environment.USE_COLOR = args.color == "always"
    Environment.DBG = args.debug
    try:
        if args.upload is not None:
            upload(args.upload)
        else:
            release(
                args.all,
                FORCED_LEVEL.get(args.force),
                args.stability,
                args.show_changelog,
            )
    except subprocess.CalledProcessError as e:
        if e.stdout:
            print(e.stdout.decode("utf-8"), file=sys.stdout)
        if e.stderr:
            print(e.stderr.decode("utf-8"), file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    __main__()
