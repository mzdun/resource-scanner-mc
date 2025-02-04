# Copyright (c) 2024 Marcin Zdun
# This code is licensed under MIT license (see LICENSE for details)

import os
import re
import urllib.parse
from typing import Callable, Dict, List, Optional, TypeVar, override

from .project import Arg, Decl, GithubInfo, Project, Version, VersionSuite
from .utils import PROJECT_ROOT

T = TypeVar("T")


class Properties(dict[str, T]):
    def __init__(self):
        super().__init__()

    def set(self, key: str, arg: T):
        self[key] = arg
        setattr(self, key, arg)


def _cleanProperties(args: Properties[Arg]):
    result: Properties[str] = Properties()
    props: Dict[str, Arg] = vars(args)
    for prop in props:
        result.set(prop, props[prop].value)
    return result


class GradleProject(Project):
    properties: Properties[str]

    @property
    @override
    def packagePrefix(self) -> str:
        return f"{self.archiveName}+"

    @property
    @override
    def packageSuffix(self) -> str:
        return ".jar"

    def __new__(cls, properties: Properties[Arg]):
        modVersion = properties.get("mod_version", Arg("", -1))
        packageRoot = properties.get("archives_base_name", Arg("", -1)).value
        url = properties.get("url", Arg("", -1)).value

        versionCoreValue = modVersion.value.split("-", 1)[0]
        versionStability = modVersion.value[len(versionCoreValue) :]

        core = Arg(versionCoreValue, modVersion.offset)
        stability = Arg(versionStability, modVersion.offset + len(versionCoreValue))
        version = Version(core, stability)

        path = urllib.parse.urlparse(url).path.split("/")[1:]
        github = None if len(path) < 2 else GithubInfo(path[0], path[1])

        ProjectClass = super(GradleProject, cls)
        self = ProjectClass.__new__(cls, packageRoot, version, github)
        self.properties = _cleanProperties(properties)
        return self

    def __repr__(self):
        projRepr = super().__repr__().split("(", 1)[1][:-1]
        return f"GradleProject({projRepr}, properties={repr(self.properties)})"


_project: GradleProject = None


def _gradleLine(off: int, line: str) -> Optional[Decl]:
    eq = re.compile(r"=\s+")
    m = eq.search(line)
    if not m:
        return None
    name = line[: m.start()].strip()
    value = line[m.end() :].strip()
    offset = off + m.end()
    return Decl(name, value, offset)


def _gradle(filename: str) -> List[Decl]:
    with open(filename, "r", encoding="UTF-8") as f:
        text = f.read()
    eol = re.compile(r"[\r\n]+")
    offset = 0
    m = eol.search(text, offset)
    decls: List[Decl] = []
    while m:
        start = m.start()
        end = m.end()
        decl = _gradleLine(offset, text[offset:start])
        if decl is not None:
            decls.append(decl)
        offset = end
        m = eol.search(text, offset)
    decl = _gradleLine(offset, text[offset:])
    if decl is not None:
        decls.append(decl)
    return decls


def _getProperties(dirName: str, conv: Callable[[Decl], T]) -> Properties[T]:
    declarations = _gradle(os.path.join(dirName, "gradle.properties"))

    result: Properties[T] = Properties()
    for decl in declarations:
        result.set(decl.name, conv(decl))

    return result


def _getPropertiesWithOffset(dirName: str) -> Properties[Arg]:
    return _getProperties(dirName, lambda x: x.asArg())


class GradleVersionSuite(VersionSuite):
    @override
    def getVersion(self) -> Project:
        global _project
        if _project is not None:
            return _project
        properties = _getPropertiesWithOffset(PROJECT_ROOT)
        _project = GradleProject(properties)
        return _project

    @override
    def patchProject(self, pos: Arg, newValue: str):
        super().patchProject(pos, newValue)
        self.reset()

    @override
    def getVersionFilePath(self):
        return os.path.join(PROJECT_ROOT, "gradle.properties")

    def reset(self):
        global _project
        _project = None
