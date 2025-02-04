# Copyright (c) 2024 Marcin Zdun
# This code is licensed under MIT license (see LICENSE for details)

"Sends binaries to latest release draft and publishes that draft"

import argparse
import hashlib
import io
import os
import sys
import time
from pprint import pformat, pprint
from typing import List

from ..common.packages import buildRegex, getPackages
from ..common.project import getVersion
from ..common.runner import Environment, print_args
from ..common.utils import PROJECT_ROOT
from .api import API


def addPublishArgumentsTo(parser: argparse.ArgumentParser):
    parser.add_argument(
        "upload",
        metavar="<dir>",
        nargs=1,
        type=str,
        help="location of the JAR files to upload",
    )


def runPublishCommand(args: argparse.Namespace):
    publishFrom(args.upload[0])


def _hash(filename: str) -> str:
    sha = hashlib.sha256()
    with open(filename, "rb") as data:
        for block in iter(lambda: data.read(io.DEFAULT_BUFFER_SIZE), b""):
            sha.update(block)
    return sha.hexdigest()


def _checksums(archive: str, names: List[str], outName: str):
    print_args(["sha256sum", "-b"])
    if True or not Environment.DRY_RUN:
        with open(os.path.join(archive, outName), "w") as output:
            for name in names:
                digest = _hash(os.path.join(archive, name))
                print(f"{digest} *{name}", file=output)
    names.append(outName)


def publishFrom(src: str):
    project = getVersion()
    api = API.fromProject(project)

    matcher = buildRegex(project)
    src, names = getPackages(src, matcher)

    if not len(names):
        print(f"no artifact matches {matcher.pattern}", file=sys.stderr)
        return None

    _checksums(src, names, "sha256sum.txt")

    releaseId = api.get_unpublished_release(project.tagName).get("id")
    if Environment.DBG:
        print(f"[DEBUG] {project.tagName=}", file=sys.stderr)
        print(f"[DEBUG] {releaseId=}", file=sys.stderr)

    if releaseId is not None:
        os.chdir(src)
        api.upload_assets(project.tagName, names)
        html_url = api.publish_release(releaseId)
        if html_url is not None:
            print(">>>", html_url, file=sys.stderr)
    elif Environment.DRY_RUN:
        if len(names):
            print(f"would upload:", file=sys.stderr)
        for name in names:
            print(f"- {name}", file=sys.stderr)
    else:
        sys.exit(1)
