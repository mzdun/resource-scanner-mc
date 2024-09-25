# Copyright (c) 2024 Marcin Zdun
# This code is licensed under MIT license (see LICENSE for details)

import os


PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(
    os.path.dirname(os.path.abspath(__file__)))))


def get_prog(name: str):
    no_main = name.split('.')[:-1]
    if len(no_main) == 0:
        return name
    if len(no_main) == 1:
        return no_main[0]

    command = no_main[-1]
    package = ".".join(no_main[:-1])
    return f"{package} {command}"
