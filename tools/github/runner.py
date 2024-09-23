# Copyright (c) 2023 Marcin Zdun
# This code is licensed under MIT license (see LICENSE for details)

import shlex
import subprocess
import sys
from typing import List, Optional, Tuple


class Environment:
    DRY_RUN: bool = False
    USE_COLOR: bool = True
    DBG: bool = False
    SECRETS: List[str] = []


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
    if not Environment.USE_COLOR:
        print(shlex.join(_hide(arg) for arg in args))
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


def checked_capture(
    *args: str, **kwargs
) -> Optional[subprocess.CompletedProcess]:
    print_args(args)
    if Environment.DRY_RUN:
        return None
    return subprocess.run(args, shell=False, capture_output=True, check=True, **kwargs)


def capture(*args: List[str], **kwargs):
    return run(*args, capture_output=True, **kwargs)
