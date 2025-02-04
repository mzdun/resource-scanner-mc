# Copyright (c) 2024 Marcin Zdun
# This code is licensed under MIT license (see LICENSE for details)

import argparse
import os
import sys
from importlib.machinery import SourceFileLoader
from types import ModuleType

__main__py = "__main__.py"

parser = argparse.ArgumentParser(
    usage="Runs some automated CI scripts", prog=__package__
)
modules = parser.add_subparsers(required=True, dest="module")

for here, dirs, files in os.walk(os.path.dirname(__file__)):
    for dirName in dirs:
        modPath = os.path.join(here, dirName, __main__py)
        if os.path.isfile(modPath):
            modules.add_parser(dirName)
    dirs[:] = []

args = parser.parse_args(sys.argv[1:2])

if "/" in args.module or "\\" in args.module:
    print(f"{args.module} is not a valid module name", file=sys.stderr)
    sys.exit(1)

main = os.path.join(os.path.dirname(__file__), args.module, __main__py)
if not os.path.isfile(main):
    print(f"{args.module} is not a valid module name", file=sys.stderr)
    sys.exit(1)

sys.argv = [main, *sys.argv[2:]]

loader = SourceFileLoader(f"{__package__}.{args.module}.__main__", main)
mod = ModuleType(loader.name)
mod.__loader__ = loader
mod.__file__ = loader.get_filename()
loader.exec_module(mod)
