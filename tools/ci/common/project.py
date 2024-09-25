# Copyright (c) 2024 Marcin Zdun
# This code is licensed under MIT license (see LICENSE for details)

from abc import ABC, abstractmethod
from typing import NamedTuple, Optional


class Arg(NamedTuple):
    value: str
    offset: int


class Decl(NamedTuple):
    name: str
    value: str
    offset: int

    def __str__(self):
        return self.value

    def asArg(self):
        return Arg(self.value, self.offset)


class Version(NamedTuple):
    core: Arg
    stability: Arg

    def __str__(self):
        return f"{self.core.value}{self.stability.value}"


class GithubInfo(NamedTuple):
    owner: str
    repo: str

    @property
    def url(self):
        return f"https://github.com/{self.owner}/{self.repo}"


class Project(NamedTuple):
    packageRoot: str
    version: Version
    github: Optional[GithubInfo]

    @property
    def archiveName(self):
        return f"{self.packageRoot}-{self.version}"

    @property
    def tagName(self):
        return f"v{self.version}"

    @property
    @abstractmethod
    def packagePrefix(self) -> str:
        pass

    @property
    @abstractmethod
    def packageSuffix(self) -> str:
        pass


class VersionSuite(ABC):
    @abstractmethod
    def getVersion(self) -> Project:
        pass

    def setVersion(self, version: str):
        core = version.split("-", 1)[0]
        stability = version[len(core):]

        self.patchProject(self.getVersion().version.core, core)

        versionPos = self.getVersion().version
        if len(stability):
            self.patchProject(versionPos.stability, stability)
        elif len(versionPos.stability.value):
            self.patchProject(versionPos.stability, "")

    @abstractmethod
    def getVersionFilePath(self):
        pass

    def patchProject(self, pos: Arg, newValue: str):
        path = self.getVersionFilePath()

        with open(path, "r", encoding="UTF-8") as input:
            text = input.read()

        patched = text[: pos.offset] + newValue + \
            text[pos.offset + len(pos.value):]

        with open(path, "w", encoding="UTF-8") as input:
            input.write(patched)


_suite: VersionSuite = None


def setVersionSuite(suite: VersionSuite):
    global _suite
    _suite = suite


def getVersion():
    return _suite.getVersion()


def setVersion(version: str):
    return _suite.setVersion(version)


def getVersionFilePath():
    return _suite.getVersionFilePath()
