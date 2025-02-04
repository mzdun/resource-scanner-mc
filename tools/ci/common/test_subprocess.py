# Copyright (c) 2024 Marcin Zdun
# This code is licensed under MIT license (see LICENSE for details)


import contextlib
import json
import shlex
import subprocess
import sys
from abc import ABC, abstractmethod
from typing import List, NamedTuple, Optional, Union, override
from unittest.mock import MagicMock, patch

DEBUG = False


class asProc(NamedTuple):
    stdout: Optional[bytes] = None
    returncode: int = 0


def asJsonBytes(data):
    return json.dumps(data, ensure_ascii=False).encode("UTF-8")


def asJsonProc(data):
    return asProc(stdout=asJsonBytes(data))


class AnyArg:
    pass


class Repeating(ABC):
    @abstractmethod
    def use(self, parent):
        pass

    @abstractmethod
    def active(self):
        pass


class Always(Repeating):
    @override
    def use(self, parent):
        if DEBUG:
            print(f"USING {parent} [always]")
        pass

    @override
    def active(self):
        return True

    def __repr__(self):
        return "always"


class Times(Repeating):
    def __init__(self, count: int):
        self.count = count
        self.orig = count

    @override
    def use(self, parent):
        self.count -= 1
        if DEBUG:
            print(f"USING {parent} [{self}]")

    @override
    def active(self):
        return self.count > 0

    def __repr__(self):
        return f"x{self.orig} (x{self.count} left)"


class Once(Times):
    def __init__(self):
        super().__init__(1)


class SubprocessMock:
    class _Matcher:
        def __init__(
            self, args: List[Union[str, AnyArg]], proc: asProc, repeating: Repeating
        ):
            self.repeating = repeating
            self.args = args
            self.proc = proc

        def __str__(self):
            return " ".join(
                (
                    shlex.quote(arg)
                    if isinstance(arg, str)
                    else "*" if isinstance(arg, AnyArg) else "?"
                )
                for arg in self.args
            )

        def matches(self, args: List[str]):
            if not self.repeating.active():
                if DEBUG:
                    print(f"      -> {self.repeating=}")
                return False

            minLen = min(len(self.args), len(args))
            if minLen < len(self.args):
                if DEBUG:
                    print(f"      -> {minLen=} < {len(self.args)=}")
                return False

            for index in range(minLen):
                m = self.args[index]
                arg = args[index]

                if isinstance(m, AnyArg):
                    continue

                if isinstance(m, str):
                    if m != arg:
                        if DEBUG:
                            print(f"      -> {m=} != {arg=}")
                        return False
                    continue

                if DEBUG:
                    print(f"      -> {m=} != {arg=}")

            return True

    class _GhMatcher:
        def __init__(self, parent: "SubprocessMock", owner: str, repo: str):
            self.parent = parent
            self.owner = owner
            self.repo = repo

        def matching(
            self,
            resource: str,
            mime: Optional[str] = None,
            method: Optional[str] = None,
            stdout: Optional[bytes] = None,
            returncode: int = 0,
            times: Repeating = Once(),
        ):
            acceptArg = f"Accept: {mime}" if mime else AnyArg()
            args: List[Union[str, AnyArg]] = [
                "gh",
                "api",
                "-H",
                acceptArg,
                "-H",
                AnyArg(),
            ]
            if method:
                args.extend(["--method", method])
            args.append(f"/repos/{self.owner}/{self.repo}{resource}")
            self.parent.matching(
                *args, stdout=stdout, returncode=returncode, times=times
            )
            return self

    def __init__(self):
        self.runMagic = MagicMock(side_effect=self.run)
        self.checkCallMagic = MagicMock(side_effect=self.checkCall)
        matchers: List[SubprocessMock._Matcher] = []
        self.matchers = matchers
        self.debug = False

    def gh(self, owner: str, repo: str):
        return SubprocessMock._GhMatcher(self, owner, repo)

    def matching(
        self,
        *args: Union[str, AnyArg],
        stdout: Optional[bytes] = None,
        returncode: int = 0,
        times: Repeating = Once(),
    ):
        self.matchers.append(
            SubprocessMock._Matcher(
                args, asProc(stdout=stdout, returncode=returncode), times
            )
        )
        if DEBUG or self.debug:
            print(f"+++ [{self.matchers[-1]}] {self.matchers[-1].repeating}")
        return self

    def _match(self, args: List[str]):
        if DEBUG or self.debug:
            print(f"\n>>> {shlex.join(args)}")
        for matcher in self.matchers:
            if DEBUG or self.debug:
                print(f"   ??? {matcher}")
            if matcher.matches(args):
                matcher.repeating.use(matcher)
                if DEBUG or self.debug:
                    print(f"   yes... [{matcher.repeating}]")
                return matcher.proc
        if DEBUG or self.debug:
            print(f"   none...")
        return None

    @contextlib.contextmanager
    def patch(self):
        with patch("subprocess.run", self.runMagic):
            with patch("subprocess.check_call", self.checkCallMagic):
                yield self

    def run(self, *args, **kwargs):
        check = kwargs.get("check", False)
        capture_output = kwargs.get("capture_output", False)
        proc = self._match(args[0])
        if proc is None:
            proc = asProc(returncode=1)

        if check and proc.returncode != 0:
            subprocess.CalledProcessError(proc.returncode, args)

        if not capture_output:
            proc = asProc(proc.returncode)

        return proc

    def checkCall(self, *args, **_):
        proc = self._match(args[0])
        if proc is None:
            proc = asProc(returncode=1)

        if proc.returncode != 0:
            subprocess.CalledProcessError(proc.returncode, args)

        return 0
