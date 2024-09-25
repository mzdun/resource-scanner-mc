# Copyright (c) 2024 Marcin Zdun
# This code is licensed under MIT license (see LICENSE for details)

from typing import List, NamedTuple, Optional
import unittest
from unittest.mock import MagicMock, patch

from .changelog import BREAKING_CHANGE, Level, CommitLink
from .git import addFiles, annotatedTag, bumpVersion, commit, getLog, getTags, setCommitSep
from .project import Arg, Project, Version
from .runner import Environment
from .test_commits import TestCommit, lorem1, lorem2, lorem3, COMMIT_SEP as MOCK_COMMIT_SEP

HASH = '5b98d7123ab5f1fcf3171a930aace8e07c50b38e'
SHORT_HASH = '5b98d7123'


class asProc(NamedTuple):
    stdout: Optional[bytes]


TAGS = b'''tag1
v0.1.0
v2.0.0
v1.0.0-alpha
v1.0.0'''


class TestGit(unittest.TestCase):
    def __init__(self, methodName):
        super().__init__(methodName)
        Environment.SILENT = True

    def test_bumpVersion(self):
        for version, level, expected in [
            ("1.99.99", Level.BENIGN, "1.99.99"),
            ("1.99.99", Level.STABILITY, "1.99.99"),
            ("1.99.99", Level.PATCH, "1.99.100"),
            ("1.99.99", Level.FEATURE, "1.100.0"),
            ("1.99.99", Level.BREAKING, "2.0.0"),
            ("1.99.99-stability.will.be.ignored", Level.BENIGN, "1.99.99"),
            ("1.99.99-stability.will.be.ignored", Level.STABILITY, "1.99.99"),
            ("1.99.99-stability.will.be.ignored", Level.PATCH, "1.99.100"),
            ("1.99.99-stability.will.be.ignored", Level.FEATURE, "1.100.0"),
            ("1.99.99-stability.will.be.ignored", Level.BREAKING, "2.0.0"),
            ("1", Level.BENIGN, "1.0.0"),
            ("1.0-alpha", Level.STABILITY, "1.0.0"),
            ("2-beta", Level.PATCH, "2.0.1"),
            ("3-rc.1", Level.FEATURE, "3.1.0"),
            ("4.5", Level.BREAKING, "5.0.0"),
        ]:
            actual = bumpVersion(version, level)
            self.assertEqual(expected, actual)

    def test_getLog_HEAD(self):
        global MOCK_COMMIT_SEP
        setCommitSep(MOCK_COMMIT_SEP)
        commit = TestCommit(HASH, simpleFixCommit)
        mock = MagicMock(return_value=asProc(
            stdout=commit.toStdout().encode('UTF-8')))
        with patch('subprocess.run', mock):
            log, level = getLog(["tag1"], {}, False)
        mock.assert_called_once_with(
            ('git', 'log', f'--format=%h %H%n%B%n{MOCK_COMMIT_SEP}', 'tag1..HEAD'), shell=False, capture_output=True)
        self.assertEqual(Level.PATCH, level)
        self.assertEqual({
            'fix': [
                CommitLink(
                    scope='',
                    summary="apply better solution for the thing that didn't work",
                    hash=HASH,
                    short_hash=SHORT_HASH,
                    is_breaking=False,
                    breaking_message=None,
                    references={'closes': [
                        'SOME OTHER THING'], 'references': ['THIS', 'THAT']}
                )
            ]
        }, log)

    def test_getLog_range(self):
        global MOCK_COMMIT_SEP
        setCommitSep(MOCK_COMMIT_SEP)
        commit = TestCommit(HASH, simpleFixCommit)
        mock = MagicMock(return_value=asProc(
            stdout=commit.toStdout().encode('UTF-8')))
        with patch('subprocess.run', mock):
            log, level = getLog(["tag1", "tag2"], {}, False)
        mock.assert_called_once_with(
            ('git', 'log', f'--format=%h %H%n%B%n{MOCK_COMMIT_SEP}', 'tag1..tag2'), shell=False, capture_output=True)
        self.assertEqual(Level.PATCH, level)
        self.assertEqual({
            'fix': [
                CommitLink(
                    scope='',
                    summary="apply better solution for the thing that didn't work",
                    hash=HASH,
                    short_hash=SHORT_HASH,
                    is_breaking=False,
                    breaking_message=None,
                    references={'closes': [
                        'SOME OTHER THING'], 'references': ['THIS', 'THAT']}
                )
            ]
        }, log)

    def test_addFiles(self):
        prev = Environment.DRY_RUN
        Environment.DRY_RUN = False
        mock = MagicMock()
        with patch('subprocess.check_call', mock):
            addFiles('A', 'B', 'C')
        Environment.DRY_RUN = prev
        mock.assert_called_once_with(
            ('git', 'add', 'A', 'B', 'C'), shell=False)

    def test_commit(self):
        prev = Environment.DRY_RUN
        Environment.DRY_RUN = False
        mock = MagicMock()
        with patch('subprocess.check_call', mock):
            commit('commit message')
        Environment.DRY_RUN = prev
        mock.assert_called_once_with(
            ('git', 'commit', '-m', 'commit message'), shell=False)

    def test_annotatedTag(self):
        prev = Environment.DRY_RUN
        Environment.DRY_RUN = False
        mock = MagicMock()
        with patch('subprocess.check_call', mock):
            annotatedTag('v1.0.0', 'commit message')
        Environment.DRY_RUN = prev
        mock.assert_called_once_with(
            ('git', 'tag', '-am', 'commit message', 'v1.0.0'), shell=False)


class TestCase(NamedTuple):
    expectedLevel: Level
    name: Optional[str] = None
    text: Optional[str] = None
    testName: Optional[str] = None
    expectedSection: Optional[str] = None
    expectedLog: Optional[CommitLink] = None
    takeAll: bool = False

    def attach(case):
        if case.name is None and case.text is None:
            raise RuntimeError(f"Both name and text cannot be None;\n{case=}")
        if case.testName is None and case.name is None:
            raise RuntimeError(
                f"Both name and testName cannot be None;\n{case=}")

        msg = globals().get(case.name) if case.text is None else case.text
        if msg is None:
            return

        def test_impl(self: TestGit):
            log, level = TestCommit.parse(
                [TestCommit(HASH, msg)], takeAll=case.takeAll)
            self.assertEqual(level, case.expectedLevel)
            if case.expectedSection is None:
                self.assertEqual(log, {})
            else:
                self.assertEqual(
                    log, {case.expectedSection: [case.expectedLog]})

        testName = f'test_{case.name}' \
            if case.testName is None else \
            f'test_{case.testName}'
        setattr(TestGit, testName, test_impl)


class GetTagsCase(NamedTuple):
    name: str
    version: str
    range: List[str]
    tags: bytes = TAGS

    def attach(case):
        def test_impl(self: TestGit):
            core = case.version.split('-', 1)[0]
            stability = case.version[len(core):]
            version = Version(Arg(core, -1), Arg(stability, -1))

            mock = MagicMock(return_value=asProc(stdout=case.tags))
            with patch('subprocess.run', mock):
                range = getTags(Project(None, version, None))
            mock.assert_called_once_with(
                ('git', 'tag'), shell=False, capture_output=True)
            self.assertEqual(case.range, range)

        testName = f'test_getTags_{case.name}'
        setattr(TestGit, testName, test_impl)


simpleFixCommit = f'''
fix: apply better solution for the thing that didn't work

{lorem1}

{lorem2}

{lorem3}

refs: THIS, THAT
closes: SOME OTHER THING
'''

simpleFixCommitNoSemCommit = f'''
fix, apply better solution for the thing that didn't work

{lorem1}

{lorem2}

{lorem3}

refs: THIS, THAT
closes: SOME OTHER THING
'''

simpleStyleCommit = "style: apply better solution for the thing that didn't work"

fixCommitNoColons = f'''
fix: apply better solution for the thing that didn't work

{lorem1}

{lorem2}

{lorem3}

refs THIS, THAT
closes SOME OTHER THING
'''

breakingNoDescriptionCommit = f'''
feat!: apply better solution for the thing that didn't work

{lorem1}

{lorem2}

{lorem3}

refs: THIS, THAT
closes: SOME OTHER THING
'''

breakingWithDescriptionCommit = f'''
feat!: apply better solution for the thing that didn't work

{lorem1}

BREAKING CHANGE:

{lorem2}

{lorem3}

refs: THIS, THAT
closes: SOME OTHER THING
'''

for test_case in [
    TestCase(
        name='simpleFixCommit',
        expectedLevel=Level.PATCH,
        expectedSection='fix',
        expectedLog=CommitLink(
            scope='',
            summary='apply better solution for the thing that didn\'t work',
            hash=HASH,
            short_hash=SHORT_HASH,
            is_breaking=False,
            breaking_message=None,
            references={
                'closes': ['SOME OTHER THING'],
                'references': ['THIS', 'THAT'],
            },
        ),
    ),
    TestCase(
        name='simpleFixCommitNoSemCommit',
        expectedLevel=Level.BENIGN,
    ),
    TestCase(
        testName='simpleFixCommitNoLogCommit',
        text='fix: (no-log) apply better solution',
        takeAll=True,
        expectedLevel=Level.BENIGN,
    ),
    TestCase(
        name='simpleStyleCommit',
        testName='simpleStyleCommit_std',
        expectedLevel=Level.BENIGN,
    ),
    TestCase(
        name='simpleStyleCommit',
        testName='simpleStyleCommit_takeAll',
        takeAll=True,
        expectedLevel=Level.BENIGN,
        expectedSection='style',
        expectedLog=CommitLink(
            scope='',
            summary='apply better solution for the thing that didn\'t work',
            hash=HASH,
            short_hash=SHORT_HASH,
            is_breaking=False,
            breaking_message=None,
            references={},
        )
    ),
    TestCase(
        testName='simpleChoreCommit',
        text='chore: menial task',
        takeAll=True,
        expectedLevel=Level.BENIGN,
        expectedSection='chore',
        expectedLog=CommitLink(
            scope='',
            summary='menial task',
            hash=HASH,
            short_hash=SHORT_HASH,
            is_breaking=False,
            breaking_message=None,
            references={},
        )
    ),
    TestCase(
        name='releaseChoreCommit',
        text='chore: release task',
        takeAll=True,
        expectedLevel=Level.BENIGN,
    ),
    TestCase(
        name='docsToFixDocsScope',
        text='docs: this is a doc fix',
        takeAll=True,
        expectedLevel=Level.PATCH,
        expectedSection='fix',
        expectedLog=CommitLink(
            scope='docs',
            summary='this is a doc fix',
            hash=HASH,
            short_hash=SHORT_HASH,
            is_breaking=False,
            breaking_message=None,
            references={},
        )
    ),
    TestCase(
        name='scopedFeature',
        text='feat(module): summary',
        takeAll=True,
        expectedLevel=Level.FEATURE,
        expectedSection='feat',
        expectedLog=CommitLink(
            scope='module',
            summary='summary',
            hash=HASH,
            short_hash=SHORT_HASH,
            is_breaking=False,
            breaking_message=None,
            references={},
        )
    ),
    TestCase(
        name='unscopedCommit',
        text='(module): summary',
        takeAll=True,
        expectedLevel=Level.BENIGN,
    ),
    TestCase(
        name='hiddenBreak_takeAll',
        text='style(module)!: summary',
        takeAll=True,
        expectedLevel=Level.BREAKING,
        expectedSection=BREAKING_CHANGE,
        expectedLog=CommitLink(
            scope='module',
            summary='summary',
            hash=HASH,
            short_hash=SHORT_HASH,
            is_breaking=True,
            breaking_message=None,
            references={},
        )
    ),
    TestCase(
        name='hiddenBreak_exposedAnyway',
        text='style(module)!: summary',
        expectedLevel=Level.BREAKING,
        expectedSection=BREAKING_CHANGE,
        expectedLog=CommitLink(
            scope='module',
            summary='summary',
            hash=HASH,
            short_hash=SHORT_HASH,
            is_breaking=True,
            breaking_message=None,
            references={},
        )
    ),
    TestCase(
        name='fixCommitNoColons',
        expectedLevel=Level.PATCH,
        expectedSection='fix',
        expectedLog=CommitLink(
            scope='',
            summary='apply better solution for the thing that didn\'t work',
            hash=HASH,
            short_hash=SHORT_HASH,
            is_breaking=False,
            breaking_message=None,
            references={},
        ),
    ),
    TestCase(
        name='breakingNoDescriptionCommit',
        expectedLevel=Level.BREAKING,
        expectedSection='feat',
        expectedLog=CommitLink(
            scope='',
            summary='apply better solution for the thing that didn\'t work',
            hash=HASH,
            short_hash=SHORT_HASH,
            is_breaking=True,
            breaking_message=None,
            references={
                'closes': ['SOME OTHER THING'],
                'references': ['THIS', 'THAT'],
            },
        ),
    ),
    TestCase(
        name='breakingWithDescriptionCommit',
        expectedLevel=Level.BREAKING,
        expectedSection='feat',
        expectedLog=CommitLink(
            scope='',
            summary='apply better solution for the thing that didn\'t work',
            hash=HASH,
            short_hash=SHORT_HASH,
            is_breaking=True,
            breaking_message=[
                'Phasellus nec mauris in mauris tincidunt gravida auctor sit amet felis. Nam eu dapibus neque. Vivamus eget odio erat. Mauris id accumsan felis. Nulla euismod feugiat nisi ac sodales. Suspendisse lacus purus, condimentum eu arcu quis, ultrices rhoncus ipsum. Curabitur ac tempus lectus.',
                'In dapibus lacinia dictum. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Curabitur ultrices lacus et dolor pretium, vitae facilisis risus cursus.'
            ],
            references={
                'closes': ['SOME OTHER THING'],
                'references': ['THIS', 'THAT'],
            },
        ),
    ),

    GetTagsCase(name='HEAD', version='2.0.1', range=['v2.0.0', 'HEAD']),
    GetTagsCase(name='empty', version='2.0.1', range=[], tags=b'tag1'),
    GetTagsCase(name='exactHEAD', version='2.0.0', range=['v2.0.0', 'HEAD']),
    GetTagsCase(name='range', version='0.1.0',
                range=['v0.1.0', 'v1.0.0-alpha']),
    GetTagsCase(name='fromAlpha', version='1.0.0-alpha',
                range=['v1.0.0-alpha', 'v1.0.0']),
    GetTagsCase(name='fromBeta', version='1.0.0-beta',
                range=['v1.0.0-alpha', 'v1.0.0']),
]:
    test_case.attach()

if __name__ == '__main__':
    unittest.main()
