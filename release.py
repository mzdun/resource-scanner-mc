#!/usr/bin/env python3
# Copyright (c) 2024 Marcin Zdun
# This code is licensed under MIT license (see LICENSE for details)

import sys
import subprocess

ARGS = ["-m", "tools.ci", "github", "release"]

retcode = subprocess.call([sys.executable, *ARGS, *sys.argv[1:]], shell=False)
sys.exit(retcode)
