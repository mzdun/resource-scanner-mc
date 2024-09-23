# Copyright (c) 2024 Marcin Zdun
# This code is licensed under MIT license (see LICENSE for details)

import os

from github.gradle import get_version

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
VERSION = get_version()

h2_prefix = '## ['
h2_current_prefix = f'## [{VERSION.ver()}]'

os.makedirs(os.path.join(ROOT, "build"), exist_ok=True)

collecting = False
lines = []
with open(os.path.join(ROOT, "CHANGELOG.md"), encoding="UTF-8") as changelog_file:
    for line in changelog_file.readlines():
        line = line.rstrip()
        is_h2 = line[:len(h2_prefix)] == h2_prefix
        if not is_h2 and collecting:
            if line[:1] == '#':
                line = f'#{line}'
            lines.append(line)
            continue
        collecting = line[:len(h2_current_prefix)] == h2_current_prefix

release_changelog = "\n".join(lines).strip()
release_changelog_path = os.path.join(ROOT, "build", "release_changelog.md")
if release_changelog == "":
    try:
        os.remove(release_changelog_path)
    except FileNotFoundError:
        pass
else:
    with open(release_changelog_path, "w", encoding="UTF-8") as release:
        print(release_changelog, file=release)
