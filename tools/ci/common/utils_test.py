# Copyright (c) 2024 Marcin Zdun
# This code is licensed under MIT license (see LICENSE for details)

import unittest
from typing import List, NamedTuple, Optional
from unittest.mock import MagicMock, patch

from .utils import get_prog


class TestUtils(unittest.TestCase):
    pass


class TestCase(NamedTuple):
    testName: str
    moduleName: str
    expectedProg: str

    def attach(case):
        def test_impl(self: TestUtils):
            actual = get_prog(case.moduleName)
            self.assertEqual(case.expectedProg, actual)

        testName = f"test_{case.testName}"
        setattr(TestUtils, testName, test_impl)


for test_case in [
    TestCase(testName="fromLeaf", moduleName="__main__", expectedProg="__main__"),
    TestCase(
        testName="fromRoot", moduleName="tools.ci.__main__", expectedProg="tools ci"
    ),
    TestCase(
        testName="fromRootPlugin",
        moduleName="tools.ci.plugin.__main__",
        expectedProg="tools.ci plugin",
    ),
    TestCase(testName="fromTools", moduleName="ci.__main__", expectedProg="ci"),
    TestCase(
        testName="fromToolsPLugin",
        moduleName="ci.plugin.__main__",
        expectedProg="ci plugin",
    ),
    TestCase(
        testName="pluginDeeper",
        moduleName="some.dir.ci.plugin.__main__",
        expectedProg="some.dir.ci plugin",
    ),
]:
    test_case.attach()

if __name__ == "__main__":
    unittest.main()
