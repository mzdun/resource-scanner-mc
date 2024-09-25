# Copyright (c) 2024 Marcin Zdun
# This code is licensed under MIT license (see LICENSE for details)

import contextlib
import io
import sys
from typing import Optional
import unittest
from unittest.mock import DEFAULT, MagicMock, call, patch

from .api import API
from ..common.project import GithubInfo, Project
from ..common.runner import Environment
from ..common.test_subprocess import Always, SubprocessMock, Times, asJsonBytes, asJsonProc, asProc


def magic_proc(stdout: Optional[bytes] = None, returncode: int = 0):
    return MagicMock(return_value=asProc(stdout=stdout, returncode=returncode))


def magic_json_proc(data):
    return magic_proc(asJsonProc(data))


@contextlib.contextmanager
def envDbg(value: bool):
    prev = Environment.DBG
    Environment.DBG = value
    yield io.StringIO()
    Environment.DBG = prev


@contextlib.contextmanager
def envSilent(value: bool):
    prev = Environment.SILENT
    Environment.SILENT = value
    yield io.StringIO()
    Environment.SILENT = prev


@contextlib.contextmanager
def dryRun(value: bool):
    prev = Environment.DRY_RUN
    Environment.DRY_RUN = value
    yield value
    Environment.DRY_RUN = prev


class TestGithubApi(unittest.TestCase):
    def __init__(self, methodName):
        super().__init__(methodName)
        self.maxDiff = None

    def test_constructor(self):
        proj = Project(None, None, GithubInfo('OWNER', 'PROJECT'))
        mock = SubprocessMock()
        mock.matching('git', 'remote', '-v',
                      stdout=b'origin\tgit@github.com:ANOTHER_OWNER/ANOTHER_PROJECT.git (fetch)\n' +
                      b'origin\tgit@github.com:ANOTHER_OWNER/ANOTHER_PROJECT.git (push)\n' +
                      b'github\tgit@github.com:OWNER/PROJECT.git (fetch)\n' +
                      b'github\tgit@github.com:OWNER/PROJECT.git (push)\n', times=Always())

        with mock.patch(), envDbg(False) as buf:
            with contextlib.redirect_stderr(buf):
                api = API.fromProject(proj)
            self.assertEqual("", buf.getvalue())

        self.assertEqual('OWNER', api.owner)
        self.assertEqual('PROJECT', api.repo)
        self.assertEqual('github', api.remote)
        self.assertEqual('https://github.com/OWNER/PROJECT', api.url)
        self.assertEqual('/repos/OWNER/PROJECT', api.root)

        with mock.patch(), envDbg(True) as buf:
            with contextlib.redirect_stderr(buf):
                api = API.fromProject(proj)
            self.assertEqual(
                "[DEBUG] remote_name='github'\n[DEBUG] url='git@github.com:OWNER/PROJECT.git'\n",
                buf.getvalue(),
            )

        with mock.patch(), envDbg(True) as buf:
            with contextlib.redirect_stderr(buf):
                api = API.fromProject(
                    Project(None, None, GithubInfo('OWNER', 'REPO')))
            self.assertEqual("", buf.getvalue())

        self.assertEqual('OWNER', api.owner)
        self.assertEqual('REPO', api.repo)
        self.assertIsNone(api.remote)

        self.assertIsNone(API.fromProject(Project(None, None, None)))

    @staticmethod
    def createApi(owner: str, repo: str):
        remotes = magic_proc(
            stdout=f'origin\tgit@github.com:{owner}/{repo}.git (push)\n'.encode('UTF-8'))
        with patch('subprocess.run', remotes):
            return API(owner, repo)

    def test_release(self):
        api = TestGithubApi.createApi('OWNER', 'REPO')
        mock = SubprocessMock()
        mock \
            .matching('git', times=Always()) \
            .gh('OWNER', 'REPO').matching('/releases',
                                          method='POST', stdout=asJsonBytes({"data": "value"}), times=Times(2))

        with mock.patch(), dryRun(True):
            api.release({
                "tag_name": "v4.5.0",
                "name": "v4.5.0",
                "body": "# Markdown",
                "draft": True,
                "prerelease": True,
            })

        with mock.patch(), dryRun(False), envDbg(True) as buf:
            with contextlib.redirect_stderr(buf):
                result = api.release({
                    "tag_name": "v5.0.0",
                    "name": "v5.0.0",
                    "body": "# Markdown",
                    "draft": True,
                    "prerelease": False,
                })

        self.assertEqual({"data": "value"}, result)
        self.assertEqual(
            "[DEBUG] stdout=\n{'data': 'value'}\n", buf.getvalue())

        mock.runMagic.assert_has_calls([
            call(('gh', 'api', '-H', 'Accept: application/vnd.github+json', '-H', 'X-GitHub-Api-Version: 2022-11-28',
                  '--method', 'POST', '/repos/OWNER/REPO/releases',
                  '-f', 'tag_name=v5.0.0',
                  '-f', 'name=v5.0.0',
                  '-f', 'body=# Markdown',
                  '-F', 'draft=true',
                  '-F', 'prerelease=false'), shell=False, capture_output=True, check=True),
        ])
        mock.checkCallMagic.assert_has_calls([
            call(('git', 'push', 'origin', 'main', '--follow-tags',
                  '--force-with-lease'), shell=False),
        ])

    def test_getUnpublishedReleases(self):
        api = TestGithubApi.createApi('OWNER', 'REPO')
        mock = SubprocessMock()
        mock.gh('OWNER', 'REPO').matching('/releases',
                                          stdout=asJsonBytes([
                                              {"data": "value",
                                                  "tag_name": "v5.0.0"},
                                              {"data": "value",
                                                  "tag_name": "v4.5.0"},
                                          ]))

        with mock.patch(), dryRun(False):
            data = api.get_unpublished_release("v4.5.0")

        self.assertEqual({"data": "value",
                          "tag_name": "v4.5.0"}, data)

        mock.runMagic.assert_has_calls([
            call(('gh', 'api', '-H', 'Accept: application/vnd.github+json', '-H', 'X-GitHub-Api-Version: 2022-11-28',
                 '/repos/OWNER/REPO/releases'), shell=False, capture_output=True, check=True),
        ])
        mock.checkCallMagic.assert_not_called()

    def test_firstFrom(self):
        api = TestGithubApi.createApi('OWNER', 'REPO')
        mock = SubprocessMock()
        mock.gh('OWNER', 'REPO').matching('/parented',
                                          times=Always(),
                                          stdout=asJsonBytes({"root": [
                                              {"data": "value",
                                                  "tag_name": "v5.0.0"},
                                              {"data": "value",
                                                  "tag_name": "v4.5.0"},
                                          ]}))

        with mock.patch(), dryRun(False):
            data = api.first_from(
                "/parented", root_name="root", tag_name="v7.1.0")

        self.assertEqual({}, data)

        mock.runMagic.assert_has_calls([
            call(('gh', 'api', '-H', 'Accept: application/vnd.github+json', '-H', 'X-GitHub-Api-Version: 2022-11-28',
                 '/repos/OWNER/REPO/parented'), shell=False, capture_output=True, check=True),
        ])
        mock.checkCallMagic.assert_not_called()

    def test_publishRelease(self):
        api = TestGithubApi.createApi('OWNER', 'REPO')
        mock = SubprocessMock()
        mock.gh('OWNER', 'REPO').matching('/releases/123',
                                          times=Always(),
                                          method="PATCH",
                                          stdout=asJsonBytes({"html_url": "http://example.com"}))

        with mock.patch(), dryRun(False):
            url = api.publish_release(123)

        self.assertEqual("http://example.com", url)

        mock.runMagic.assert_has_calls([
            call(('gh', 'api', '-H', 'Accept: application/vnd.github+json', '-H', 'X-GitHub-Api-Version: 2022-11-28',
                  '--method', 'PATCH', '/repos/OWNER/REPO/releases/123',
                  '-f', 'draft=false',
                  '-F', 'make_latest=legacy'), shell=False, capture_output=True, check=True),
        ])
        mock.checkCallMagic.assert_not_called()

    def test_uploadAssets(self):
        api = TestGithubApi.createApi('OWNER', 'REPO')
        mock = SubprocessMock()
        mock.matching('gh', 'release', 'upload',
                      times=Always(),
                      returncode=0)

        with mock.patch(), dryRun(False):
            api.upload_assets('v1.2.3', ['a', 'b', 'c'])

        mock.runMagic.assert_not_called()
        mock.checkCallMagic.assert_has_calls(
            [call(('gh', 'release', 'upload', 'v1.2.3', 'a', 'b', 'c', '--clobber'), shell=False)])
