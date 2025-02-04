#!/usr/bin/env python3
# Copyright (c) 2024 Marcin Zdun
# This code is licensed under MIT license (see LICENSE for details)

"""
link-compat <from> <to>
"""

import os
import shutil
import sys
import xml.etree.ElementTree as ET

from cache import Cache

__root__ = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


def __main__():
    fromVersion = sys.argv[1]
    toVersion = sys.argv[2]

    dirnameTo = os.path.join(__root__, "fabric", toVersion)

    def symlink(*subdirs: str):
        link = subdirs[-1]
        subdirs = subdirs[:-1]
        localDirname = os.path.join(dirnameTo, *subdirs)
        target = os.path.join(
            *([".."] * len(subdirs)), "..", fromVersion, *subdirs, link
        )
        os.makedirs(localDirname, exist_ok=True)
        os.chdir(localDirname)
        os.symlink(target, link, target_is_directory=True)

    shutil.rmtree(os.path.join(dirnameTo, "src", "main", "java", "api", "compat"))
    symlink("src", "main", "java", "api", "compat")


if __name__ == "__main__":
    sys.exit(__main__())
