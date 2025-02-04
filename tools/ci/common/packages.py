# Copyright (c) 2024 Marcin Zdun
# This code is licensed under MIT license (see LICENSE for details)

import os
import re
import zipfile

from .project import Project


def safeRegex(value: str) -> str:
    for esc in "\\.+*?()[]":
        value = value.replace(esc, f"\\{esc}")
    return value


def getPackages(src: str, matcher: re.Pattern):
    if os.path.isdir(src):
        for _, dirnames, filenames in os.walk(src):
            dirnames[:] = []
            names = [name for name in filenames if matcher.match(name)]
    else:
        nextSrc = f"{src}-dir"
        os.makedirs(nextSrc, exist_ok=True)
        with zipfile.ZipFile(src) as zip:
            names = [name for name in zip.namelist() if matcher.match(name)]
            for name in names:
                zip.extract(name, path=nextSrc)
        src = nextSrc

    return src, names


def buildRegex(project: Project):
    regexPre = safeRegex(project.packagePrefix)
    regexPost = safeRegex(project.packageSuffix)
    regex = f"^{regexPre}(.*){regexPost}$"
    return re.compile(regex)
