# Copyright (c) 2023 Marcin Zdun
# This code is licensed under MIT license (see LICENSE for details)

import os
import secrets
import string
from typing import Dict, List, Tuple
from .runner import capture, checked

from .changelog import (
    BREAKING_CHANGE,
    ISSUE_LINKS,
    KNOWN_TYPES,
    LEVEL_BENIGN,
    LEVEL_BREAKING,
    LEVEL_FEATURE,
    LEVEL_PATCH,
    TYPE_FIX,
    ChangeLog,
    Commit,
    CommitLink,
)
from .cmake import Project

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

COMMIT_SEP = "--{}".format(
    "".join(secrets.choice(string.ascii_letters + string.digits) for i in range(20))
)


def _sem_ver(tag):
    split = tag.split("-", 1)
    if len(split) == 2:
        stability = split[1]
    else:
        stability = ""
    ver = [int(s) for s in split[0].split(".")]
    while len(ver) < 3:
        ver.append(0)
    return (*ver, stability)


def _level_from_commit(commit: Commit) -> Tuple[int, str]:
    if commit.is_breaking:
        return (LEVEL_BREAKING, commit.scope)
    try:
        current_type = TYPE_FIX[commit.type]
        current_scope = commit.type
    except KeyError:
        current_type = commit.type
        current_scope = commit.scope
    current_type = {"feat": LEVEL_FEATURE, "fix": LEVEL_PATCH}.get(current_type, LEVEL_BENIGN)
    return (current_type, current_scope)


def _get_commit(hash: str, short_hash: str, message: str) -> Commit:
    subject, body = (message + "\n\n").split("\n\n", 1)
    split = subject.split(": ", 1)
    if len(split) != 2:
        return None

    encoded, summary = split
    encoded = encoded.strip()
    is_breaking = len(encoded) and encoded[-1] == "!"
    if is_breaking:
        encoded = encoded[:-1].rstrip()
    type_scope = encoded.split("(", 1)
    if not len(type_scope[0]):
        return None
    scope = ""
    if len(type_scope) == 2:
        scope = ")".join(type_scope[1].split(")")[:-1]).strip()

    breaking_change = None
    references = {}
    body = body.strip().split("BREAKING CHANGE:", 1)
    if len(body) > 1:
        breaking_change = [para.strip() for para in body[1].strip().split("\n\n")]

    lines = body[0].strip().split("\n")
    for index_plus_1 in range(len(lines), 0, -1):
        index = index_plus_1 - 1
        footer_line = lines[index].strip()
        if footer_line == "":
            continue
        footer = footer_line.split(": ", 1)
        if len(footer) == 1:
            break
        name = footer[0].strip().lower()
        if name in ISSUE_LINKS:
            items = [v.strip() for v in footer[1].split(",")]
            key = ISSUE_LINKS[name]
            if key not in references:
                references[key] = []
            references[key] = items + references[key]
            continue

    return Commit(
        type_scope[0].strip(),
        scope,
        summary,
        hash,
        short_hash,
        is_breaking,
        breaking_change,
        references,
    )


def get_tags(version: Project) -> List[str]:
    tags = capture("git", "tag").stdout.decode("UTF-8").split("\n")
    versions = []
    for tag in tags:
        if tag[:1] != "v":
            continue
        value = [*_sem_ver(tag[1:])]
        if value[3] == "":
            value[3] = "z"
        versions.append([(*value,), tag])
    versions = list(reversed(sorted(versions)))

    curr = [*_sem_ver(version.ver())]
    if curr[3] == "":
        curr[3] = "z"
    ver = (*curr,)
    for index in range(len(versions)):
        if versions[index][0] > ver:
            continue
        if index > 0:
            return [versions[index][1], versions[index - 1][1]]
        return [versions[index][1], "HEAD"]
    return []


def get_log(
    commit_range: List[str], scope_fix: Dict[str, str], take_all: bool
) -> Tuple[ChangeLog, int]:
    args = ["git", "log", f"--format=%h %H%n%B%n{COMMIT_SEP}"]
    if len(commit_range):
        if len(commit_range) == 1:
            args.append(f"{commit_range[0]}..HEAD")
        else:
            args.append("..".join(commit_range[:2]))
    proc = capture(*args)
    commit_log = []
    amassed = []
    for line in proc.stdout.decode("UTF-8").split("\n"):
        if line == COMMIT_SEP:
            if len(amassed):
                short_hash, hash = amassed[0].split(" ")
                commit = _get_commit(hash, short_hash, "\n".join(amassed[1:]).strip())
                amassed = []

                if commit is None:
                    continue

                commit_log.append(commit)
            continue
        amassed.append(line)

    changes: ChangeLog = {}
    level = LEVEL_BENIGN

    for commit in commit_log:
        # Hide even from --all
        if commit.type == "chore" and commit.summary[:8] == "release ":
            continue
        if "(no-log)" in commit.summary:
            continue
        current_level, current_scope = _level_from_commit(commit)
        if current_level > level:
            level = current_level
        current_type = TYPE_FIX.get(commit.type, commit.type)
        hidden = current_type not in KNOWN_TYPES

        if hidden and not commit.is_breaking and not take_all:
            continue
        if hidden and commit.is_breaking:
            current_type = BREAKING_CHANGE

        current_scope = scope_fix.get(current_scope, current_scope)
        if current_type not in changes:
            changes[current_type] = []
        changes[current_type].append(
            CommitLink(
                current_scope,
                commit.summary,
                commit.hash,
                commit.short_hash,
                commit.is_breaking,
                commit.breaking_message,
                commit.references,
            )
        )

    return changes, level


def bump_version(ver: str, level: int):
    semver = [*_sem_ver(ver)]
    if level > LEVEL_BENIGN:
        lvl = LEVEL_BREAKING - level
        semver[lvl] += 1
        for index in range(lvl + 1, len(semver)):
            semver[index] = 0
    return ".".join(str(v) for v in semver[:-1])


def add_files(*files: str):
    checked("git", "add", *files)


def commit(message: str):
    checked("git", "commit", "-m", message)


def annotated_tag(new_tag: str, message: str):
    checked("git", "tag", "-am", message, new_tag)
