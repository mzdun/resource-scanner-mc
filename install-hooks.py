#!/usr/bin/env python3

with open(".git/hooks/pre-commit", "w", encoding="UTF-8") as script:
    script.write('''
#!/bin/sh

TOPDIR=$(git rev-parse --show-toplevel)

cd "$TOPDIR"
exec python -m tools.ci code license --check-staged --silent
'''.strip())
