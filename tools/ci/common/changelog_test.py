# Copyright (c) 2024 Marcin Zdun
# This code is licensed under MIT license (see LICENSE for details)

import unittest
from typing import NamedTuple, Optional, cast
from unittest.mock import MagicMock, mock_open, patch

from ..github.api import format_release
from .changelog import (ChangelogFileUpdate, GithubReleaseChangelog,
                        format_commit_message, release_changelog,
                        update_changelog)
from .test_commits import TestCommit, lorem1, lorem2, lorem3


class asProc(NamedTuple):
    stdout: Optional[bytes] = None
    returncode: int = 0


HASH_A = "5b98d7123ab5f1fcf3171a930aace8e07c50b38e"
HASH_B = "d9fae2db7c1a4c1b97111c0a7a8332e82f84151a"
HASH_C = "18baa4d1c18e88559971a2d108479562419bfa46"
HASH_D = "53a9e3b134bacf5baf795373c74428e22127ff29"
HASH_E = "71e0e98bcea67b2c0ccd3f3a5099564d67243b30"
HASH_F = "8ed444e77a8954cf70c273b40f0b9a0d5d0ef21a"
HASH_G = "b17dd3e0c375230d6c8bf4993b95a17fe55895e3"
HASH_H = "69988295cd0999b6b028901645dd06199fb65b6c"
HASH_I = "b9b1c858e2c109ec12e845935018f4f080b9cb9f"


breakingFeatureCommit = f"""
feat!: new version coming in

{lorem1}

BREAKING CHANGE:
{lorem2}

{lorem3}

refs: THIS, THAT, HERE, THERE
closes: SOME OTHER THING, #123
"""


class TestChangelog(unittest.TestCase):
    def __init__(self, methodName):
        super().__init__(methodName)
        self.maxDiff = None

    def test_empty(self):
        log, _ = TestCommit.parse([])
        self.assertEqual("", format_commit_message(log))

    def _getLog(self, takeAll: bool = False):
        log, _ = TestCommit.parse(
            [
                TestCommit(HASH_A, breakingFeatureCommit),
                TestCommit(
                    HASH_B,
                    "fix: change one thing, with very long synopsis, much longer then the 78 limit per line, not saying anything about 50",
                ),
                TestCommit(HASH_C, "fix: support another thing"),
                TestCommit(HASH_D, "fix: provide final thing"),
                TestCommit(
                    HASH_E, "feat(engine): change something in the engine, I guess"
                ),
                TestCommit(HASH_F, "style(sass): update the theme"),
                TestCommit(
                    HASH_G,
                    "custom section(new scope): that one is strange\n\nFixes: #1456",
                ),
                TestCommit(HASH_H, "fix(layout)!: break the getPos"),
                TestCommit(
                    HASH_I,
                    "ci(github actions)!: update ubuntu runners to ubuntu-latest",
                ),
            ],
            takeAll=takeAll,
        )
        return log

    def test_formatting_commitMessage(self):
        log = self._getLog()
        self.assertEqual(
            """

Breaking:

 - github actions: update ubuntu runners to ubuntu-latest (b9b1c858e)

New Features:

 - breaking: new version coming in (5b98d7123), closes SOME OTHER THING and
   #123, references THIS, THAT, HERE and THERE
 - engine: change something in the engine, I guess (71e0e98bc)

Bug Fixes:

 - change one thing, with very long synopsis, much longer then the 78 limit
   per line, not saying anything about 50 (d9fae2db7)
 - support another thing (18baa4d1c)
 - provide final thing (53a9e3b13)
 - breaking, layout: break the getPos (69988295c)

BREAKING CHANGES:

Phasellus nec mauris in mauris tincidunt gravida auctor sit amet felis. Nam eu
dapibus neque. Vivamus eget odio erat. Mauris id accumsan felis. Nulla euismod
feugiat nisi ac sodales. Suspendisse lacus purus, condimentum eu arcu quis,
ultrices rhoncus ipsum. Curabitur ac tempus lectus.

In dapibus lacinia dictum. Class aptent taciti sociosqu ad litora torquent per
conubia nostra, per inceptos himenaeos. Curabitur ultrices lacus et dolor
pretium, vitae facilisis risus cursus.""",
            format_commit_message(log),
        )

    def test_formatting_changelog(self):
        log = self._getLog()
        self.assertEqual(
            """
## [5.0.0]({GITHUB}/compare/v4.78.1258...v5.0.0) (YYYY-MM-DD)

### Breaking

- **github actions**: update ubuntu runners to ubuntu-latest ([b9b1c858e]({GITHUB}/commit/b9b1c858e2c109ec12e845935018f4f080b9cb9f))

### New Features

- **breaking**: new version coming in ([5b98d7123]({GITHUB}/commit/5b98d7123ab5f1fcf3171a930aace8e07c50b38e)), closes SOME OTHER THING and [#123]({GITHUB}/issues/123), references THIS, THAT, HERE and THERE
- **engine**: change something in the engine, I guess ([71e0e98bc]({GITHUB}/commit/71e0e98bcea67b2c0ccd3f3a5099564d67243b30))

### Bug Fixes

- change one thing, with very long synopsis, much longer then the 78 limit per line, not saying anything about 50 ([d9fae2db7]({GITHUB}/commit/d9fae2db7c1a4c1b97111c0a7a8332e82f84151a))
- support another thing ([18baa4d1c]({GITHUB}/commit/18baa4d1c18e88559971a2d108479562419bfa46))
- provide final thing ([53a9e3b13]({GITHUB}/commit/53a9e3b134bacf5baf795373c74428e22127ff29))
- **breaking, layout**: break the getPos ([69988295c]({GITHUB}/commit/69988295cd0999b6b028901645dd06199fb65b6c))

### BREAKING CHANGES

Phasellus nec mauris in mauris tincidunt gravida auctor sit amet felis. Nam eu dapibus neque. Vivamus eget odio erat. Mauris id accumsan felis. Nulla euismod feugiat nisi ac sodales. Suspendisse lacus purus, condimentum eu arcu quis, ultrices rhoncus ipsum. Curabitur ac tempus lectus.

In dapibus lacinia dictum. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Curabitur ultrices lacus et dolor pretium, vitae facilisis risus cursus.
""".lstrip(),
            ChangelogFileUpdate(
                "{GITHUB}", "v5.0.0", "v4.78.1258", "YYYY-MM-DD"
            ).format_changelog(log),
        )

    def test_formatting_release(self):
        log = self._getLog()
        self.assertEqual(
            {
                "tag_name": "v5.0.0",
                "name": "v5.0.0",
                "body": """
### Breaking

- **github actions**: update ubuntu runners to ubuntu-latest ([b9b1c858e]({GITHUB}/commit/b9b1c858e2c109ec12e845935018f4f080b9cb9f))

### New Features

- **breaking**: new version coming in ([5b98d7123]({GITHUB}/commit/5b98d7123ab5f1fcf3171a930aace8e07c50b38e)), closes SOME OTHER THING and #123, references THIS, THAT, HERE and THERE
- **engine**: change something in the engine, I guess ([71e0e98bc]({GITHUB}/commit/71e0e98bcea67b2c0ccd3f3a5099564d67243b30))

### Bug Fixes

- change one thing, with very long synopsis, much longer then the 78 limit per line, not saying anything about 50 ([d9fae2db7]({GITHUB}/commit/d9fae2db7c1a4c1b97111c0a7a8332e82f84151a))
- support another thing ([18baa4d1c]({GITHUB}/commit/18baa4d1c18e88559971a2d108479562419bfa46))
- provide final thing ([53a9e3b13]({GITHUB}/commit/53a9e3b134bacf5baf795373c74428e22127ff29))
- **breaking, layout**: break the getPos ([69988295c]({GITHUB}/commit/69988295cd0999b6b028901645dd06199fb65b6c))

### BREAKING CHANGES

Phasellus nec mauris in mauris tincidunt gravida auctor sit amet felis. Nam eu dapibus neque. Vivamus eget odio erat. Mauris id accumsan felis. Nulla euismod feugiat nisi ac sodales. Suspendisse lacus purus, condimentum eu arcu quis, ultrices rhoncus ipsum. Curabitur ac tempus lectus.

In dapibus lacinia dictum. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Curabitur ultrices lacus et dolor pretium, vitae facilisis risus cursus.

**Full Changelog**: {GITHUB}/compare/v4.78.1258...v5.0.0
""".strip(),
                "draft": True,
                "prerelease": False,
            },
            format_release(log, "v5.0.0", "v4.78.1258", "{GITHUB}"),
        )

    def test_formatting_commitMessage_takeAll(self):
        log = self._getLog(takeAll=True)
        self.assertEqual(
            """

Breaking:

 - github actions: update ubuntu runners to ubuntu-latest (b9b1c858e)

New Features:

 - breaking: new version coming in (5b98d7123), closes SOME OTHER THING and
   #123, references THIS, THAT, HERE and THERE
 - engine: change something in the engine, I guess (71e0e98bc)

Bug Fixes:

 - change one thing, with very long synopsis, much longer then the 78 limit
   per line, not saying anything about 50 (d9fae2db7)
 - support another thing (18baa4d1c)
 - provide final thing (53a9e3b13)
 - breaking, layout: break the getPos (69988295c)

custom section:

 - new scope: that one is strange (b17dd3e0c), fixes #1456

Code Style:

 - sass: update the theme (8ed444e77)

BREAKING CHANGES:

Phasellus nec mauris in mauris tincidunt gravida auctor sit amet felis. Nam eu
dapibus neque. Vivamus eget odio erat. Mauris id accumsan felis. Nulla euismod
feugiat nisi ac sodales. Suspendisse lacus purus, condimentum eu arcu quis,
ultrices rhoncus ipsum. Curabitur ac tempus lectus.

In dapibus lacinia dictum. Class aptent taciti sociosqu ad litora torquent per
conubia nostra, per inceptos himenaeos. Curabitur ultrices lacus et dolor
pretium, vitae facilisis risus cursus.""",
            format_commit_message(log),
        )

    def test_formatting_changelog_takeAll(self):
        log = self._getLog(takeAll=True)
        self.assertEqual(
            """
## [5.0.0]({GITHUB}/compare/v4.78.1258...v5.0.0) (YYYY-MM-DD)

### Breaking

- **github actions**: update ubuntu runners to ubuntu-latest ([b9b1c858e]({GITHUB}/commit/b9b1c858e2c109ec12e845935018f4f080b9cb9f))

### New Features

- **breaking**: new version coming in ([5b98d7123]({GITHUB}/commit/5b98d7123ab5f1fcf3171a930aace8e07c50b38e)), closes SOME OTHER THING and [#123]({GITHUB}/issues/123), references THIS, THAT, HERE and THERE
- **engine**: change something in the engine, I guess ([71e0e98bc]({GITHUB}/commit/71e0e98bcea67b2c0ccd3f3a5099564d67243b30))

### Bug Fixes

- change one thing, with very long synopsis, much longer then the 78 limit per line, not saying anything about 50 ([d9fae2db7]({GITHUB}/commit/d9fae2db7c1a4c1b97111c0a7a8332e82f84151a))
- support another thing ([18baa4d1c]({GITHUB}/commit/18baa4d1c18e88559971a2d108479562419bfa46))
- provide final thing ([53a9e3b13]({GITHUB}/commit/53a9e3b134bacf5baf795373c74428e22127ff29))
- **breaking, layout**: break the getPos ([69988295c]({GITHUB}/commit/69988295cd0999b6b028901645dd06199fb65b6c))

### custom section

- **new scope**: that one is strange ([b17dd3e0c]({GITHUB}/commit/b17dd3e0c375230d6c8bf4993b95a17fe55895e3)), fixes [#1456]({GITHUB}/issues/1456)

### Code Style

- **sass**: update the theme ([8ed444e77]({GITHUB}/commit/8ed444e77a8954cf70c273b40f0b9a0d5d0ef21a))

### BREAKING CHANGES

Phasellus nec mauris in mauris tincidunt gravida auctor sit amet felis. Nam eu dapibus neque. Vivamus eget odio erat. Mauris id accumsan felis. Nulla euismod feugiat nisi ac sodales. Suspendisse lacus purus, condimentum eu arcu quis, ultrices rhoncus ipsum. Curabitur ac tempus lectus.

In dapibus lacinia dictum. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Curabitur ultrices lacus et dolor pretium, vitae facilisis risus cursus.
""".lstrip(),
            ChangelogFileUpdate(
                "{GITHUB}", "v5.0.0", "v4.78.1258", "YYYY-MM-DD"
            ).format_changelog(log),
        )

    def test_formatting_release_takeAll(self):
        log = self._getLog(takeAll=True)
        self.assertEqual(
            {
                "tag_name": "v5.0.0-beta",
                "name": "v5.0.0-beta",
                "body": """
### Breaking

- **github actions**: update ubuntu runners to ubuntu-latest ([b9b1c858e]({GITHUB}/commit/b9b1c858e2c109ec12e845935018f4f080b9cb9f))

### New Features

- **breaking**: new version coming in ([5b98d7123]({GITHUB}/commit/5b98d7123ab5f1fcf3171a930aace8e07c50b38e)), closes SOME OTHER THING and #123, references THIS, THAT, HERE and THERE
- **engine**: change something in the engine, I guess ([71e0e98bc]({GITHUB}/commit/71e0e98bcea67b2c0ccd3f3a5099564d67243b30))

### Bug Fixes

- change one thing, with very long synopsis, much longer then the 78 limit per line, not saying anything about 50 ([d9fae2db7]({GITHUB}/commit/d9fae2db7c1a4c1b97111c0a7a8332e82f84151a))
- support another thing ([18baa4d1c]({GITHUB}/commit/18baa4d1c18e88559971a2d108479562419bfa46))
- provide final thing ([53a9e3b13]({GITHUB}/commit/53a9e3b134bacf5baf795373c74428e22127ff29))
- **breaking, layout**: break the getPos ([69988295c]({GITHUB}/commit/69988295cd0999b6b028901645dd06199fb65b6c))

### custom section

- **new scope**: that one is strange ([b17dd3e0c]({GITHUB}/commit/b17dd3e0c375230d6c8bf4993b95a17fe55895e3)), fixes #1456

### Code Style

- **sass**: update the theme ([8ed444e77]({GITHUB}/commit/8ed444e77a8954cf70c273b40f0b9a0d5d0ef21a))

### BREAKING CHANGES

Phasellus nec mauris in mauris tincidunt gravida auctor sit amet felis. Nam eu dapibus neque. Vivamus eget odio erat. Mauris id accumsan felis. Nulla euismod feugiat nisi ac sodales. Suspendisse lacus purus, condimentum eu arcu quis, ultrices rhoncus ipsum. Curabitur ac tempus lectus.

In dapibus lacinia dictum. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Curabitur ultrices lacus et dolor pretium, vitae facilisis risus cursus.

**Full Changelog**: {GITHUB}/compare/v4.78.1258...v5.0.0-beta
""".strip(),
                "draft": True,
                "prerelease": True,
            },
            format_release(log, "v5.0.0-beta", "v4.78.1258", "{GITHUB}"),
        )

    def test_addFirstEntryToChangelog(self):
        openMock = cast(MagicMock, mock_open(read_data="# Changelog\n"))
        runMock = MagicMock(return_value=asProc(stdout=b"YYYY-MM-DD"))
        log = self._getLog()
        with patch("builtins.open", openMock), patch("subprocess.run", runMock):
            update_changelog(log, "v5.0.0", "v4.78.1258", "{GITHUB}")

        openMock.return_value.write.assert_called_once_with(
            b"""# Changelog

## [5.0.0]({GITHUB}/compare/v4.78.1258...v5.0.0) (YYYY-MM-DD)

### Breaking

- **github actions**: update ubuntu runners to ubuntu-latest ([b9b1c858e]({GITHUB}/commit/b9b1c858e2c109ec12e845935018f4f080b9cb9f))

### New Features

- **breaking**: new version coming in ([5b98d7123]({GITHUB}/commit/5b98d7123ab5f1fcf3171a930aace8e07c50b38e)), closes SOME OTHER THING and [#123]({GITHUB}/issues/123), references THIS, THAT, HERE and THERE
- **engine**: change something in the engine, I guess ([71e0e98bc]({GITHUB}/commit/71e0e98bcea67b2c0ccd3f3a5099564d67243b30))

### Bug Fixes

- change one thing, with very long synopsis, much longer then the 78 limit per line, not saying anything about 50 ([d9fae2db7]({GITHUB}/commit/d9fae2db7c1a4c1b97111c0a7a8332e82f84151a))
- support another thing ([18baa4d1c]({GITHUB}/commit/18baa4d1c18e88559971a2d108479562419bfa46))
- provide final thing ([53a9e3b13]({GITHUB}/commit/53a9e3b134bacf5baf795373c74428e22127ff29))
- **breaking, layout**: break the getPos ([69988295c]({GITHUB}/commit/69988295cd0999b6b028901645dd06199fb65b6c))

### BREAKING CHANGES

Phasellus nec mauris in mauris tincidunt gravida auctor sit amet felis. Nam eu dapibus neque. Vivamus eget odio erat. Mauris id accumsan felis. Nulla euismod feugiat nisi ac sodales. Suspendisse lacus purus, condimentum eu arcu quis, ultrices rhoncus ipsum. Curabitur ac tempus lectus.

In dapibus lacinia dictum. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Curabitur ultrices lacus et dolor pretium, vitae facilisis risus cursus.
"""
        )

    def test_addNextEntryToChangelog(self):
        openMock = cast(
            MagicMock, mock_open(read_data="# Changelog\n\n## Previous release")
        )
        runMock = MagicMock(return_value=asProc(returncode=1))
        log = self._getLog()
        with patch("builtins.open", openMock), patch("subprocess.run", runMock), patch(
            "time.strftime", MagicMock(return_value="TODAY")
        ):
            update_changelog(log, "v5.0.0", "v4.78.1258", "{GITHUB}")

        openMock.return_value.write.assert_called_once_with(
            b"""# Changelog

## [5.0.0]({GITHUB}/compare/v4.78.1258...v5.0.0) (TODAY)

### Breaking

- **github actions**: update ubuntu runners to ubuntu-latest ([b9b1c858e]({GITHUB}/commit/b9b1c858e2c109ec12e845935018f4f080b9cb9f))

### New Features

- **breaking**: new version coming in ([5b98d7123]({GITHUB}/commit/5b98d7123ab5f1fcf3171a930aace8e07c50b38e)), closes SOME OTHER THING and [#123]({GITHUB}/issues/123), references THIS, THAT, HERE and THERE
- **engine**: change something in the engine, I guess ([71e0e98bc]({GITHUB}/commit/71e0e98bcea67b2c0ccd3f3a5099564d67243b30))

### Bug Fixes

- change one thing, with very long synopsis, much longer then the 78 limit per line, not saying anything about 50 ([d9fae2db7]({GITHUB}/commit/d9fae2db7c1a4c1b97111c0a7a8332e82f84151a))
- support another thing ([18baa4d1c]({GITHUB}/commit/18baa4d1c18e88559971a2d108479562419bfa46))
- provide final thing ([53a9e3b13]({GITHUB}/commit/53a9e3b134bacf5baf795373c74428e22127ff29))
- **breaking, layout**: break the getPos ([69988295c]({GITHUB}/commit/69988295cd0999b6b028901645dd06199fb65b6c))

### BREAKING CHANGES

Phasellus nec mauris in mauris tincidunt gravida auctor sit amet felis. Nam eu dapibus neque. Vivamus eget odio erat. Mauris id accumsan felis. Nulla euismod feugiat nisi ac sodales. Suspendisse lacus purus, condimentum eu arcu quis, ultrices rhoncus ipsum. Curabitur ac tempus lectus.

In dapibus lacinia dictum. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Curabitur ultrices lacus et dolor pretium, vitae facilisis risus cursus.

## Previous release"""
        )

    def test_releaseChangelog_preset(self):
        mock = cast(
            MagicMock,
            mock_open(
                read_data="""# Changelog
## [5.0.1]

## [5.0.0]

### New Features

- **engine**: change something in the engine, I guess ([71e0e98bc]({GITHUB}/commit/71e0e98bcea67b2c0ccd3f3a5099564d67243b30))

### Bug Fixes

- change one thing, with very long synopsis, much longer then the 78 limit per line, not saying anything about 50 ([d9fae2db7]({GITHUB}/commit/d9fae2db7c1a4c1b97111c0a7a8332e82f84151a))
- support another thing ([18baa4d1c]({GITHUB}/commit/18baa4d1c18e88559971a2d108479562419bfa46))
- provide final thing ([53a9e3b13]({GITHUB}/commit/53a9e3b134bacf5baf795373c74428e22127ff29))
- **breaking, layout**: break the getPos ([69988295c]({GITHUB}/commit/69988295cd0999b6b028901645dd06199fb65b6c))

## [4.1.0]

"""
            ),
        )

        with patch("builtins.open", mock):
            result = release_changelog("5.0.0")
        self.assertEqual(
            """
**New Features**

- **engine**: change something in the engine, I guess ([71e0e98bc]({GITHUB}/commit/71e0e98bcea67b2c0ccd3f3a5099564d67243b30))

**Bug Fixes**

- change one thing, with very long synopsis, much longer then the 78 limit per line, not saying anything about 50 ([d9fae2db7]({GITHUB}/commit/d9fae2db7c1a4c1b97111c0a7a8332e82f84151a))
- support another thing ([18baa4d1c]({GITHUB}/commit/18baa4d1c18e88559971a2d108479562419bfa46))
- provide final thing ([53a9e3b13]({GITHUB}/commit/53a9e3b134bacf5baf795373c74428e22127ff29))
- **breaking, layout**: break the getPos ([69988295c]({GITHUB}/commit/69988295cd0999b6b028901645dd06199fb65b6c))
""".strip(),
            result,
        )

    def test_releaseChangelog_absent(self):
        mock = cast(
            MagicMock,
            mock_open(
                read_data="""# Changelog
## [5.0.1]

## [5.0.0]

### New Features

- **engine**: change something in the engine, I guess ([71e0e98bc]({GITHUB}/commit/71e0e98bcea67b2c0ccd3f3a5099564d67243b30))

### Bug Fixes

- change one thing, with very long synopsis, much longer then the 78 limit per line, not saying anything about 50 ([d9fae2db7]({GITHUB}/commit/d9fae2db7c1a4c1b97111c0a7a8332e82f84151a))
- support another thing ([18baa4d1c]({GITHUB}/commit/18baa4d1c18e88559971a2d108479562419bfa46))
- provide final thing ([53a9e3b13]({GITHUB}/commit/53a9e3b134bacf5baf795373c74428e22127ff29))
- **breaking, layout**: break the getPos ([69988295c]({GITHUB}/commit/69988295cd0999b6b028901645dd06199fb65b6c))

## [4.1.0]

"""
            ),
        )

        with patch("builtins.open", mock):
            result = release_changelog("4.5.0")
        self.assertIsNone(result)


if __name__ == "__main__":
    unittest.main()
