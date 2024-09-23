# Copyright (c) 2024 Marcin Zdun
# This code is licensed under MIT license (see LICENSE for details)

import os
import re
from typing import List, NamedTuple, Optional

PROJECT_SOURCE_DIRECTORY = os.path.dirname(
    os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
)


class Arg(NamedTuple):
    value: str
    offset: int


class Decl(NamedTuple):
    name: str
    value: str
    offset: int

    def asArg(self):
        return Arg(self.value, self.offset)


class Project(NamedTuple):
    package_name: Arg
    repo: Arg
    version: Arg
    stability: Arg
    description: Arg

    def ver(self):
        return f"{self.version.value}{self.stability.value}"

    def pkg(self):
        return f"{self.package_name.value}-{self.ver()}"

    def tag(self):
        return f"v{self.ver()}"


_project_version: Project = None


def _gradle_line(off: int, line: str) -> Optional[Decl]:
    eq = re.compile(r"=\s+")
    m = eq.search(line)
    if not m:
        return None
    name = line[:m.start()].strip()
    value = line[m.end():].strip()
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
        decl = _gradle_line(offset, text[offset:start])
        if decl is not None:
            decls.append(decl)
        offset = end
        m = eol.search(text, offset)
    decl = _gradle_line(offset, text[offset:])
    if decl is not None:
        decls.append(decl)
    return decls


def get_version() -> Project:
    global _project_version
    if _project_version is not None:
        return _project_version
    declarations = _gradle(os.path.join(
        PROJECT_SOURCE_DIRECTORY, "gradle.properties"))

    package_name: Optional[Arg] = None
    repo: Optional[Arg] = None
    version = Arg("0.1.0", -1)
    version_stability: Optional[Arg] = None
    description = Arg("", -1)

    for decl in declarations:
        if decl.name == "archives_base_name":
            package_name = decl.asArg()
        elif decl.name == "url":
            url = decl.value
            repo_name = url.split('/')[-1]
            repo = Arg(repo_name, decl.offset + len(url) - len(repo_name))
        elif decl.name == "mod_version":
            mod_version = decl.asArg()
            split_version = mod_version.value.split('-', 1)
            if len(split_version) == 2:
                version_value = split_version[0]
                version = Arg(version_value, mod_version.offset)
                version_stability = Arg(
                    mod_version.value[len(version_value):], mod_version.offset + len(version_value))
            else:
                version = mod_version
                version_stability = None
        elif decl.name == "description":
            description = decl.asArg()

    if package_name is None:
        package_name = Arg("", -1)
    if repo is None:
        repo = package_name
    if version_stability is None:
        version_stability = Arg("", -1)

    _project_version = Project(
        package_name=package_name,
        version=version,
        stability=version_stability,
        description=description,
        repo=repo,
    )

    return _project_version


def _patch(arg: Arg, value: str):
    global _project_version

    with open(
        os.path.join(PROJECT_SOURCE_DIRECTORY, "gradle.properties"), "r", encoding="UTF-8"
    ) as input:
        text = input.read()

    patched = text[: arg.offset] + value + text[arg.offset + len(arg.value):]

    with open(
        os.path.join(PROJECT_SOURCE_DIRECTORY, "gradle.properties"), "w", encoding="UTF-8"
    ) as input:
        input.write(patched)

    _project_version = None


def set_version(ver: str):
    ver_split = ver.split("-", 1)
    ver = ver_split[0]

    _patch(get_version().version, ver)
    if len(ver_split) > 1:
        stability = f"-{ver_split[1]}"
        _patch(get_version().stability, stability)
    elif len(get_version().stability.value):
        _patch(get_version().stability, "")
