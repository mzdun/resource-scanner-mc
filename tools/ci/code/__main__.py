# Copyright (c) 2024 Marcin Zdun
# This code is licensed under MIT license (see LICENSE for details)

"Performs mechanical operations on code"

import argparse
import datetime
import os
import re
import subprocess
import sys
from typing import Set

from ..common.packages import safeRegex
from ..common.runner import Environment
from ..common.utils import PROJECT_ROOT, get_prog

PACKAGE = "com.midnightbits.scanner"
SRC_SETS = ["main", "test"]

EXT = '.java'

MASK = '''
// Copyright (c) {YEAR} Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)
'''.strip()

exit_code = 0

license_doc = 'Adds missing license references'

parser = argparse.ArgumentParser(
    description=__doc__,
    prog=get_prog(__name__))

commands = parser.add_subparsers(required=True, dest='command')
license = commands.add_parser(
    'license', help=license_doc, description=license_doc)

Environment.addArgumentsTo(license)


def licenses():
    ROOT = os.path.join(PROJECT_ROOT, "")
    PKG = os.path.join(*PACKAGE.split("."))

    FILENAME_REPLACEMENT = [(os.path.join("src", srcSet, "java", PKG),
                             os.path.join(f"${srcSet.upper()}", "$PKG")) for srcSet in SRC_SETS]

    maskChunks = MASK.split('{YEAR}')
    reMaskContentsLines = r'\d+'.join([safeRegex(chunk)
                                      for chunk in maskChunks]).split('\n')
    reMaskContents = r'[\r\n]+'.join(reMaskContentsLines)
    matcher = re.compile(f'^{reMaskContents}$')

    year = str(datetime.date.today().year)
    newHeading = year.join(maskChunks) + "\n\n"

    # files: Set[str] = set()
    for root, _, filenames in os.walk(PROJECT_ROOT):
        for filename in filenames:
            if filename[-len(EXT):] != EXT:
                continue
            path = os.path.join(root, filename)

            with open(path, encoding="UTF-8") as inFile:
                text = inFile.readlines()
            head = '\n'.join([line.rstrip() for line in text[:2]])

            m = matcher.match(head)
            if m is not None:
                continue

            printPath = path[len(ROOT):]
            for replacement in FILENAME_REPLACEMENT:
                printPath = printPath.replace(*replacement)
            print(printPath)

            newText = newHeading + "".join(text)
            with open(path, "wb") as outFile:
                outFile.write(newText.encode('UTF-8'))


def __main__():
    args = parser.parse_args()
    Environment.apply(args)

    try:
        licenses()
    except subprocess.CalledProcessError as e:
        if e.stdout:
            print(e.stdout.decode("utf-8"), file=sys.stdout)
        if e.stderr:
            print(e.stderr.decode("utf-8"), file=sys.stderr)
        sys.exit(1)
    sys.exit(exit_code)


__main__()
