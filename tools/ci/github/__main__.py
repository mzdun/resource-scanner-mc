# Copyright (c) 2024 Marcin Zdun
# This code is licensed under MIT license (see LICENSE for details)


import argparse
import subprocess
import sys
from typing import Callable, Dict

from .publish import addPublishArgumentsTo, runPublishCommand, __doc__ as __publish_doc__
from .release import addReleaseArgumentsTo, runReleaseCommand, __doc__ as __release_doc__
from ..common.changelog import FORCED_LEVEL
from ..common.runner import Environment
from ..common.utils import get_prog

parser = argparse.ArgumentParser(
    description="Works with GitHub releases",
    prog=get_prog(__name__))
commands = parser.add_subparsers(required=True, dest='command')
release_command = commands.add_parser(
    'release', help=__release_doc__, description=__release_doc__)
publish_command = commands.add_parser(
    'publish', help=__publish_doc__, description=__publish_doc__)

Environment.addArgumentsTo(release_command)
addReleaseArgumentsTo(release_command)

Environment.addArgumentsTo(publish_command)
addPublishArgumentsTo(publish_command)


COMMANDS: Dict[str, Callable[[argparse.Namespace], None]] = {
    'release': runReleaseCommand,
    'publish': runPublishCommand
}


def __main__():
    args = parser.parse_args()
    Environment.apply(args)
    call = COMMANDS.get(args.command)
    if call is None:
        print(f'''Internal error: don't know how to run: {
              args.command}''', file=sys.stderr)
        sys.exit(1)

    try:
        call(args)
    except subprocess.CalledProcessError as e:
        if e.stdout:
            print(e.stdout.decode("utf-8"), file=sys.stdout)
        if e.stderr:
            print(e.stderr.decode("utf-8"), file=sys.stderr)
        sys.exit(1)


__main__()
