# Copyright (c) 2024 Marcin Zdun
# This code is licensed under MIT license (see LICENSE for details)

"Performs mechanical operations on code"

import argparse
import datetime
import os
import re
import subprocess
import sys
from typing import List, Set

from ..common.packages import safeRegex
from ..common.runner import Environment, capture
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

license.add_argument(
    "--check",
    action="store_true",
    required=False,
    help="verifies, if all Java files have the notification",
)

license.add_argument(
    "--check-staged",
    action="store_true",
    required=False,
    help="verifies, if all staged Java files have the notification",
)

def build_regexp():
    maskChunks = MASK.split('{YEAR}')
    reMaskContentsLines = r'\d+'.join([safeRegex(chunk)
                                      for chunk in maskChunks]).split('\n')
    reMaskContents = r'[\r\n]+'.join(reMaskContentsLines)
    matcher = re.compile(f'^{reMaskContents}$')

    year = str(datetime.date.today().year)
    newHeading = year.join(maskChunks) + "\n\n"

    return (matcher, newHeading)


def matches(matcher: re.Pattern[str], text: List[str]):
    head = '\n'.join([line.rstrip() for line in text[:2]])
    return matcher.match(head) is not None


def check_staged():
    proc = capture('git','diff','--name-status','--cached')
    lines = proc.stdout.decode("UTF-8").split("\n")
    filenames = [line[1:].strip() for line in lines if line[:1] != 'D']
    matcher, _ = build_regexp()

    files: Set[str] = set()
    for path in filenames:
        if path[-len(EXT):] != EXT:
            continue
        
        proc = capture('git', 'show', f':{path}')
        if not matches(matcher, proc.stdout.decode("UTF-8").split("\n")):
            files.add(path)

    if len(files) > 0:
        print(f'Found {len(files)} file{"s" if len(files) != 1 else ""} without license notification:\n', file=sys.stderr)
        for file in sorted(files):
            print('-', file, file=sys.stderr)

        print(f'\nTo fix {"them" if len(files) != 1 else "it"}, run\n\n    python -m tools.ci code license\n\nand stage them again.', file=sys.stderr)
        exit(1)


def licenses(check: bool):
    ROOT = os.path.join(PROJECT_ROOT, "")
    PKG = os.path.join(*PACKAGE.split("."))

    FILENAME_REPLACEMENT = [(os.path.join("src", srcSet, "java", PKG),
                             os.path.join(f"${srcSet.upper()}", "$PKG")) for srcSet in SRC_SETS]

    matcher, newHeading = build_regexp()

    files: Set[str] = set()
    for root, _, filenames in os.walk(PROJECT_ROOT):
        for filename in filenames:
            if filename[-len(EXT):] != EXT:
                continue
            path = os.path.join(root, filename)

            with open(path, encoding="UTF-8") as inFile:
                text = inFile.readlines()
                
            if matches(matcher, text):
                continue
            
            if check:
                files.add(path[len(ROOT):])
                continue

            printPath = path[len(ROOT):]
            for replacement in FILENAME_REPLACEMENT:
                printPath = printPath.replace(*replacement)
            print(printPath)

            newText = newHeading + "".join(text)
            with open(path, "wb") as outFile:
                outFile.write(newText.encode('UTF-8'))

    if check and len(files) > 0:
        print(f'Found {len(files)} file{"s" if len(files) != 1 else ""} without license notification:\n', file=sys.stderr)
        for file in sorted(files):
            print('-', file, file=sys.stderr)

        print(f'\nTo fix {"them" if len(files) != 1 else "it"}, run\n\n    python -m tools.ci code license\n', file=sys.stderr)
        exit(1)

def __main__():
    args = parser.parse_args()
    Environment.apply(args)

    try:
        if args.check_staged:
            check_staged()
        else:
            licenses(args.check)
    except subprocess.CalledProcessError as e:
        if e.stdout:
            print(e.stdout.decode("utf-8"), file=sys.stdout)
        if e.stderr:
            print(e.stderr.decode("utf-8"), file=sys.stderr)
        sys.exit(1)
    sys.exit(exit_code)


__main__()
