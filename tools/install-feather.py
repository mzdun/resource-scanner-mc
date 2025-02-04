#!/usr/bin/env python3
# Copyright (c) 2024 Marcin Zdun
# This code is licensed under MIT license (see LICENSE for details)

import os
import shutil
import sys

HOME = os.environ.get("HOME")
AppData = os.environ.get("AppData")

rootDir = HOME if HOME is not None else AppData
rootRepl = "~" if HOME is not None else "%AppData%"

userMods = os.path.join(".feather", "user-mods", sys.argv[1])
modDir = os.path.join(rootDir, userMods)

srcFile = sys.argv[2]
pluginName = os.path.basename(srcFile)
dstFile = os.path.join(modDir, pluginName)

os.makedirs(modDir, exist_ok=True)
shutil.copy2(srcFile, dstFile)

print(f"Copied {pluginName} to {os.path.join(rootRepl, userMods)}")
