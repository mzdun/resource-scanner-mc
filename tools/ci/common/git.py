# Copyright (c) 2023 Marcin Zdun
# This code is licensed under MIT license (see LICENSE for details)

import os
import re
import secrets
import string
from typing import Dict, List, Tuple
from .runner import capture, checked

from .changelog import (
    BREAKING_CHANGE,
    ISSUE_LINKS,
    KNOWN_TYPES,
    Level,
    TYPE_FIX,
    ChangeLog,
    Commit,
    CommitLink,
)
from .project import Project

ROOT = os.path.dirname(os.path.dirname(
    os.path.dirname(os.path.abspath(__file__))))

COMMIT_SEP = "--{}".format(
    "".join(secrets.choice(string.ascii_letters + string.digits)
            for i in range(20))
)


def setCommitSep(sep: str):
    global COMMIT_SEP
    COMMIT_SEP = sep


def _semVer(tag: str):
    split = tag.split("-", 1)
    if len(split) == 2:
        stability = split[1]
    else:
        stability = ""
    ver = [int(s) for s in split[0].split(".")]
    while len(ver) < 3:
        ver.append(0)
    return (*ver, stability)


def _levelFromCommit(commit: Commit) -> Tuple[Level, str]:
    if commit.is_breaking:
        return (Level.BREAKING, commit.scope)
    try:
        currentType = TYPE_FIX[commit.type]
        currentScope = commit.type
    except KeyError:
        currentType = commit.type
        currentScope = commit.scope
    currentLevel = {"feat": Level.FEATURE, "fix": Level.PATCH}.get(
        currentType, Level.BENIGN)
    return (currentLevel, currentScope)


def _getCommit(hash: str, shortHash: str, message: str) -> Commit:
    subject, body = (message + "\n\n").split("\n\n", 1)
    split = subject.split(": ", 1)
    if len(split) != 2:
        return None

    encoded, summary = split
    encoded = encoded.strip()
    isBreaking = len(encoded) and encoded[-1] == "!"
    if isBreaking:
        encoded = encoded[:-1].rstrip()
    typeScope = encoded.split("(", 1)
    if not len(typeScope[0]):
        return None
    scope = ""
    if len(typeScope) == 2:
        scope = ")".join(typeScope[1].split(")")[:-1]).strip()

    references = {}
    lines = body.rstrip().split("\n")
    for indexPlus1 in range(len(lines), 0, -1):
        index = indexPlus1 - 1
        footerLine = lines[index].strip()
        if footerLine == "":
            lines = lines[:-1]
            continue
        footer = footerLine.split(": ", 1)
        if len(footer) == 1:
            break
        lines = lines[:-1]
        name = footer[0].strip().lower()
        if name in ISSUE_LINKS:
            items = [v.strip() for v in footer[1].split(",")]
            key = ISSUE_LINKS[name]
            if key not in references:
                references[key] = []
            references[key] = items + references[key]
            continue

    breakingChange = None
    body = "\n".join(lines).strip().split("BREAKING CHANGE", 1)
    if len(body) > 1:
        body = body[1].lstrip(':').strip()
        breakingChange = [re.sub(r"\s+", " ", para.strip())
                          for para in body.split("\n\n")]

    return Commit(
        typeScope[0].strip(),
        scope,
        summary,
        hash,
        shortHash,
        isBreaking,
        breakingChange,
        references,
    )


def getTags(project: Project) -> List[str]:
    tags: List[str] = capture("git", "tag").stdout.decode("UTF-8").split("\n")
    versions = []
    for tag in tags:
        if tag[:1] != "v":
            continue
        value = [*_semVer(tag[1:])]
        if value[3] == "":
            value[3] = "z"
        versions.append(((*value,), tag))
    versions: List[Tuple[Tuple[int, int, int, str], str]] = list(
        reversed(sorted(versions)))

    curr = [*_semVer(str(project.version))]
    if curr[3] == "":
        curr[3] = "z"
    ver = (*curr,)
    for index in range(len(versions)):
        currVer, tag = versions[index]
        if currVer > ver:
            continue
        if index > 0:
            return [tag, versions[index - 1][1]]
        return [tag, "HEAD"]
    return []


def getLog(
    commitRange: List[str], scopeFix: Dict[str, str], takeAll: bool
) -> Tuple[ChangeLog, Level]:
    args = ["git", "log", f"--format=%h %H%n%B%n{COMMIT_SEP}"]
    if len(commitRange):
        if len(commitRange) == 1:
            args.append(f"{commitRange[0]}..HEAD")
        else:
            args.append("..".join(commitRange[:2]))
    proc = capture(*args)
    return parseLog(proc.stdout.decode("UTF-8"), COMMIT_SEP, scopeFix, takeAll)


def parseLog(gitLogOutput: str, separator: str, scopeFix: Dict[str, str], takeAll: bool):
    commitLog = []
    amassed = []
    for line in gitLogOutput.split("\n"):
        if line == separator:
            if len(amassed):
                shortHash, hash = amassed[0].split(" ")
                commit = _getCommit(
                    hash, shortHash, "\n".join(amassed[1:]).strip())
                amassed = []

                if commit is None:
                    continue

                commitLog.append(commit)
            continue
        amassed.append(line)

    changes: ChangeLog = {}
    level = Level.BENIGN

    for commit in commitLog:
        # Hide even from --all
        if commit.type == "chore" and commit.summary[:8] == "release ":
            continue
        if "(no-log)" in commit.summary:
            continue
        current_level, current_scope = _levelFromCommit(commit)
        if current_level.value > level.value:
            level = current_level
        current_type = TYPE_FIX.get(commit.type, commit.type)
        hidden = current_type not in KNOWN_TYPES

        if hidden and not commit.is_breaking and not takeAll:
            continue
        if hidden and commit.is_breaking:
            current_type = BREAKING_CHANGE

        current_scope = scopeFix.get(current_scope, current_scope)
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


def bumpVersion(ver: str, level: Level):
    semver = [*_semVer(ver)]
    if level.value > Level.STABILITY.value:
        lvl = Level.BREAKING.value - level.value
        semver[lvl] += 1
        for index in range(lvl + 1, len(semver)):
            semver[index] = 0
    return ".".join(str(v) for v in semver[:-1])


def addFiles(*files: str):
    checked("git", "add", *files)


def commit(message: str):
    checked("git", "commit", "-m", message)


def annotatedTag(newTag: str, message: str):
    checked("git", "tag", "-am", message, newTag)
