# Copyright (c) 2024 Marcin Zdun
# This code is licensed under MIT license (see LICENSE for details)

import argparse
import contextlib
import io
from typing import List
import unittest
from unittest.mock import MagicMock, patch

from .runner import Environment, checked, checked_capture, run


def setupEnvironment(*args: str):
    parser = argparse.ArgumentParser()
    Environment.addArgumentsTo(parser)
    result = parser.parse_args(args)
    # print(result)
    Environment.apply(result)
    Environment.SECRETS = []


def setupEnv(mockedCall: str = 'subprocess.check_call', dryRun: bool = False, useColor: bool = False, debug: bool = False, silent: bool = False):
    args: List[str] = []
    if dryRun:
        args.append("--dry-run")
    if not useColor:
        args.append("--color")
        args.append("never")
    if debug:
        args.append("--debug")
    if silent:
        args.append("--silent")
    setupEnvironment(*args)

    return patch(mockedCall, MagicMock())


class TestRunner(unittest.TestCase):
    def test_run(self):
        with setupEnv(mockedCall='subprocess.run') as mock, io.StringIO() as buf:
            with contextlib.redirect_stderr(buf):
                run("app", "arg1", "arg2", "arg 3", "--flag")
            mock.assert_called_once_with(
                ('app', 'arg1', 'arg2', 'arg 3', '--flag'), shell=False)
            self.assertEqual("app arg1 arg2 'arg 3' --flag\n", buf.getvalue())

    def test_dryRun_run(self):
        with setupEnv(mockedCall='subprocess.run', dryRun=True, useColor=True) as mock, io.StringIO() as buf:
            with contextlib.redirect_stderr(buf):
                run("app", "arg1", "arg2", "arg 3", "--flag")
            mock.assert_called_once_with(
                ('app', 'arg1', 'arg2', 'arg 3', '--flag'), shell=False)
            self.assertEqual(
                "\x1b[33mapp\x1b[m arg1 arg2 \x1b[2;34m'arg 3'\x1b[m \x1b[2;37m--flag\x1b[m\n", buf.getvalue())

    def test_dryRun_checked(self):
        with setupEnv(dryRun=True) as mock, io.StringIO() as buf:
            with contextlib.redirect_stderr(buf):
                checked("app", "arg1", "arg2", "arg 3", "--flag")
            mock.assert_not_called()
            self.assertEqual("app arg1 arg2 'arg 3' --flag\n", buf.getvalue())

    def test_silent_checked(self):
        with setupEnv(silent=True) as mock, io.StringIO() as buf:
            with contextlib.redirect_stderr(buf):
                checked("app", "arg1", "arg2", "arg 3", "--flag")
            mock.assert_called_once_with(
                ('app', 'arg1', 'arg2', 'arg 3', '--flag'), shell=False)
            self.assertEqual("", buf.getvalue())

    def test_dryRun_noColor_checkedCapture(self):
        with setupEnv(mockedCall='subprocess.run', dryRun=True) as mock, io.StringIO() as buf:
            with contextlib.redirect_stderr(buf):
                checked_capture("app", "arg1", "arg2", "arg 3", "--flag")
            mock.assert_not_called()
            self.assertEqual("app arg1 arg2 'arg 3' --flag\n", buf.getvalue())

    def test_secret_run_checkedCapture(self):
        with setupEnv(mockedCall='subprocess.run', useColor=True) as mock, io.StringIO() as buf:
            Environment.SECRETS = ['arg 3']
            with contextlib.redirect_stderr(buf):
                checked_capture("app", "arg1", "arg2", "arg 3", "--flag")
            mock.assert_called_once_with(
                ('app', 'arg1', 'arg2', 'arg 3', '--flag'), shell=False, capture_output=True, check=True)
            self.assertEqual(
                "\x1b[33mapp\x1b[m arg1 arg2 \x1b[2;34m'?????'\x1b[m \x1b[2;37m--flag\x1b[m\n", buf.getvalue())

    def test_dryRun_noColor_secret_checked(self):
        with setupEnv(dryRun=True) as mock, io.StringIO() as buf:
            Environment.SECRETS = ['arg 3']
            with contextlib.redirect_stderr(buf):
                checked("app", "arg1", "arg2", "arg 3", "--flag")
            mock.assert_not_called()
            self.assertEqual("app arg1 arg2 '?????' --flag\n", buf.getvalue())
