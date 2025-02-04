# Copyright (c) 2023 Marcin Zdun
# This code is licensed under MIT license (see LICENSE for details)

import argparse
import shlex
import subprocess
import sys
from typing import List, Optional, Tuple


class Environment:
    DRY_RUN: bool = False
    USE_COLOR: bool = True
    DBG: bool = False
    SILENT: bool = False
    SECRETS: List[str] = []

    @staticmethod
    def addArgumentsTo(parser: argparse.ArgumentParser):
        parser.add_argument(
            "--dry-run",
            action="store_true",
            required=False,
            help="print commands, change nothing",
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
        parser.add_argument(
            "--silent",
            required=False,
            action="store_true",
        )

    @staticmethod
    def apply(args: argparse.Namespace):
        Environment.DRY_RUN = args.dry_run
        Environment.USE_COLOR = args.color == "always"
        Environment.DBG = args.debug
        Environment.SILENT = args.silent


def _hide(arg: str):
    for secret in Environment.SECRETS:
        arg = arg.replace(secret, "?" * len(secret))
    return arg


def _print_arg(arg: str):
    color = ""
    arg = _hide(arg)
    if arg[:1] == "-":
        color = "\033[2;37m"
    arg = shlex.join([arg])
    if color == "" and arg[:1] in ["'", '"']:
        color = "\033[2;34m"
    if color == "":
        return arg
    return f"{color}{arg}\033[m"


def print_args(args: Tuple[str]):
    if Environment.SILENT:
        return

    if not Environment.USE_COLOR:
        print(shlex.join(_hide(arg) for arg in args), file=sys.stderr)
        return

    cmd = shlex.join([args[0]])
    args = " ".join([_print_arg(arg) for arg in args[1:]])
    print(f"\033[33m{cmd}\033[m {args}", file=sys.stderr)


def run(*args: str, **kwargs) -> subprocess.CompletedProcess:
    print_args(args)
    return subprocess.run(args, shell=False, **kwargs)


def checked(*args: str, **kwargs) -> Optional[subprocess.CompletedProcess]:
    print_args(args)
    if Environment.DRY_RUN:
        return None
    return subprocess.check_call(args, shell=False, **kwargs)


def checked_capture(*args: str, **kwargs) -> Optional[subprocess.CompletedProcess]:
    print_args(args)
    if Environment.DRY_RUN:
        return None
    return subprocess.run(args, shell=False, capture_output=True, check=True, **kwargs)


def capture(*args: str, **kwargs):
    return run(*args, capture_output=True, **kwargs)


def capture_str(*args: str, **kwargs):
    return capture(*args, **kwargs).stdout.decode("UTF-8").strip()
