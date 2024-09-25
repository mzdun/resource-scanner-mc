# Copyright (c) 2024 Marcin Zdun
# This code is licensed under MIT license (see LICENSE for details)

import os
from typing import cast, override
import unittest
from unittest.mock import MagicMock, call, mock_open, patch

from .gradle import GradleVersionSuite
from .project import Arg, Decl, GithubInfo, Project, Version, VersionSuite, getVersion, getVersionFilePath, setVersion, setVersionSuite
from .utils import PROJECT_ROOT


class AlmostAbstract(VersionSuite):
    @override
    def getVersion(self) -> Project:
        return super().getVersion()

    @override
    def getVersionFilePath(self):
        return super().getVersionFilePath()


class TestGradle(unittest.TestCase):
    def __init__(self, methodName):
        super().__init__(methodName)
        self.maxDiff = None

    def test_touchDeclReprAndStr(self):
        decl = Decl('name', 'value', 123)
        self.assertEqual('value', str(decl))
        self.assertEqual(
            "Decl(name='name', value='value', offset=123)", repr(decl))

    def test_abstractMethods(self):
        project = Project(None, None, None)
        version = AlmostAbstract()
        self.assertIsNone(project.packagePrefix)
        self.assertIsNone(project.packageSuffix)
        self.assertIsNone(version.getVersion())
        self.assertIsNone(version.getVersionFilePath())

    def test_getVersion(self):
        mock1 = cast(MagicMock, mock_open(read_data='''mod_version = 1.99.5061-alpha.5
archives_base_name = ARCHIVE
url = https://github.com/OWNER/PROJECT/PACKAGE
'''))
        mock2 = cast(MagicMock, mock_open(read_data=''))
        suite = GradleVersionSuite()
        suite.reset()
        setVersionSuite(suite)
        with patch('builtins.open', mock1):
            project = getVersion()
        with patch('builtins.open', mock2):
            secondProject = getVersion()

        self.assertEqual(project.packageRoot, "ARCHIVE")
        self.assertEqual(project.version, Version(
            Arg("1.99.5061", 14), Arg("-alpha.5", 23)))
        self.assertEqual(project.github, GithubInfo("OWNER", "PROJECT"))
        self.assertEqual(project.github.url,
                         "https://github.com/OWNER/PROJECT")
        self.assertEqual(project.archiveName, "ARCHIVE-1.99.5061-alpha.5")
        self.assertEqual(project.tagName, "v1.99.5061-alpha.5")
        self.assertEqual(project.packagePrefix, "ARCHIVE-1.99.5061-alpha.5+")
        self.assertEqual(project.packageSuffix, ".jar")
        self.assertEqual(repr(
            project), "GradleProject(packageRoot='ARCHIVE', version=Version(core=Arg(value='1.99.5061', offset=14), stability=Arg(value='-alpha.5', offset=23)), github=GithubInfo(owner='OWNER', repo='PROJECT'), properties={'mod_version': '1.99.5061-alpha.5', 'archives_base_name': 'ARCHIVE', 'url': 'https://github.com/OWNER/PROJECT/PACKAGE'})")
        self.assertEqual(repr(project), repr(secondProject))
        mock1.assert_called_once_with(os.path.join(
            PROJECT_ROOT, 'gradle.properties'), 'r', encoding='UTF-8')
        mock2.assert_not_called()

    def test_getVersion_stable(self):
        mock = cast(MagicMock, mock_open(read_data='''mod_version = 1.99.5061
archives_base_name = ARCHIVE
url = https://github.com/OWNER/PROJECT/PACKAGE'''))
        suite = GradleVersionSuite()
        suite.reset()
        setVersionSuite(suite)
        with patch('builtins.open', mock):
            project = getVersion()
        self.assertEqual(repr(
            project), "GradleProject(packageRoot='ARCHIVE', version=Version(core=Arg(value='1.99.5061', offset=14), stability=Arg(value='', offset=23)), github=GithubInfo(owner='OWNER', repo='PROJECT'), properties={'mod_version': '1.99.5061', 'archives_base_name': 'ARCHIVE', 'url': 'https://github.com/OWNER/PROJECT/PACKAGE'})")

    def test_getVersionFilePath(self):
        suite = GradleVersionSuite()
        setVersionSuite(suite)
        self.assertEqual(os.path.join(
            PROJECT_ROOT, "gradle.properties"), getVersionFilePath())

    def test_setVersion_core(self):
        mock = cast(MagicMock, mock_open(read_data='''mod_version = 1.99.5061
archives_base_name = ARCHIVE
url = https://github.com/OWNER/PROJECT/PACKAGE'''))
        suite = GradleVersionSuite()
        suite.reset()
        setVersionSuite(suite)
        with patch('builtins.open', mock):
            setVersion("1.101.0")
        mock.return_value.write.assert_called_once_with(
            '''mod_version = 1.101.0
archives_base_name = ARCHIVE
url = https://github.com/OWNER/PROJECT/PACKAGE''')

    def test_setVersion_stability(self):
        mock = cast(MagicMock, mock_open(read_data='''mod_version = 1.99.5061
archives_base_name = ARCHIVE
url = https://github.com/OWNER/PROJECT/PACKAGE
'''))
        suite = GradleVersionSuite()
        suite.reset()
        setVersionSuite(suite)
        with patch('builtins.open', mock):
            setVersion("1.101.0-alpha.5")
        mock.return_value.write.assert_has_calls(
            [
                call('''mod_version = 1.101.0
archives_base_name = ARCHIVE
url = https://github.com/OWNER/PROJECT/PACKAGE
'''),
                call('''mod_version = 1.99.5061-alpha.5
archives_base_name = ARCHIVE
url = https://github.com/OWNER/PROJECT/PACKAGE
'''),
            ]
        )

    def test_setVersion_clearStability(self):
        mock = cast(MagicMock, mock_open(read_data='''mod_version = 1.99.5061-rc.1
archives_base_name = ARCHIVE
url = https://github.com/OWNER/PROJECT/PACKAGE
'''))
        suite = GradleVersionSuite()
        suite.reset()
        setVersionSuite(suite)
        with patch('builtins.open', mock):
            setVersion("1.101.0")
        mock.return_value.write.assert_has_calls(
            [
                call('''mod_version = 1.101.0-rc.1
archives_base_name = ARCHIVE
url = https://github.com/OWNER/PROJECT/PACKAGE
'''),
                call('''mod_version = 1.99.5061
archives_base_name = ARCHIVE
url = https://github.com/OWNER/PROJECT/PACKAGE
'''),
            ]
        )
