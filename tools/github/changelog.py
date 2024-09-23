# Copyright (c) 2023 Marcin Zdun
# This code is licensed under MIT license (see LICENSE for details)

import time
import os
import re
from typing import Dict, List, NamedTuple
from .runner import capture

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))


class Section(NamedTuple):
    key: str
    header: str


LEVEL_BENIGN = 0
LEVEL_STABILITY = 1
LEVEL_PATCH = 2
LEVEL_FEATURE = 3
LEVEL_BREAKING = 4

FORCED_LEVEL = {
    "patch": LEVEL_PATCH,
    "fix": LEVEL_PATCH,
    "minor": LEVEL_FEATURE,
    "feat": LEVEL_FEATURE,
    "feature": LEVEL_FEATURE,
    "major": LEVEL_BREAKING,
    "breaking": LEVEL_BREAKING,
    "release": LEVEL_BREAKING,
    "stability": LEVEL_STABILITY,
}

BREAKING_CHANGE = "BREAKING_CHANGE"
TYPES = [
    Section(BREAKING_CHANGE, "Breaking"),
    Section("feat", "New Features"),
    Section("fix", "Bug Fixes"),
]
KNOWN_TYPES = [section.key for section in TYPES]
TYPE_FIX = {"docs": "fix"}
ALL_TYPES = {
    "assets": "Assets",
    "build": "Build System",
    "chore": "Chores",
    "ci": "Continuous Integration",
    "perf": "Performance Improvements",
    "refactor": "Code Refactoring",
    "revert": "Reverts",
    "style": "Code Style",
    "test": "Tests",
}
ISSUE_LINKS = {"refs": "references", "closes": "closes", "fixes": "fixes"}


class Commit(NamedTuple):
    type: str
    scope: str
    summary: str
    hash: str
    short_hash: str
    is_breaking: bool
    breaking_message: List[str] = []
    references: Dict[str, List[str]] = {}


class CommitLink(NamedTuple):
    scope: str
    summary: str
    hash: str
    short_hash: str
    is_breaking: bool
    breaking_message: List[str]
    references: Dict[str, List[str]]


ChangeLog = Dict[str, List[CommitLink]]


def _issue_link(github_link: str, ref: str) -> str:
    if ref[:1] == "#" and ref != "#" and ref[1:].isdigit():
        return f"[{ref}]({github_link}/issues/{ref[1:]})"
    return ref


def _link_str(
    github_link: str, link: CommitLink, show_breaking: bool, for_github: bool
) -> str:
    scope = ""
    if len(link.scope):
        scope = link.scope

    if show_breaking:
        if link.is_breaking:
            if len(scope):
                scope = f"breaking, {scope}"
            else:
                scope = f"breaking"

    if len(scope):
        scope = f"**{scope}**: "

    hash_link = f"{github_link}/commit/{link.hash}"

    refs = ""
    conv = str if for_github else (lambda ref: _issue_link(github_link, ref))
    for refs_name in link.references:
        refs_links = [
            conv(issue) for issue in link.references[refs_name] if issue != ""
        ]
        if len(refs_links) > 0:
            refs += f", {refs_name} "
            if len(refs_links) == 1:
                refs += refs_links[0]
            else:
                last = refs_links[-1]
                listed = ", ".join(refs_links[:-1])
                refs += f"{listed} and {last}"

    return f"- {scope}{link.summary} ([{link.short_hash}]({hash_link})){refs}"


def _show_links(
    github_link: str, links: List[CommitLink], show_breaking: bool, for_github: bool
) -> List[str]:
    issues: Dict[str, List[str]] = {}
    for link in links:
        scope = link.scope
        if scope not in issues:
            issues[scope] = []
        issues[scope].append(_link_str(github_link, link, show_breaking, for_github))
    result = []
    for scope in sorted(issues.keys()):
        result.extend(issues[scope])
    if len(result):
        result.append("")
    return result


def _find_breaking_notes(links: List[CommitLink]) -> List[str]:
    breaking: List[str] = []
    for link in links:
        if link.breaking_message is not None:
            paras = []
            for para in link.breaking_message:
                text = re.sub(r"\s+", " ", para.strip())
                if text != "":
                    paras.append(text + "\n")
            if len(paras):
                breaking.extend(paras)
    return breaking


def format_changelog(
    log: ChangeLog,
    cur_tag: str,
    prev_tag: str,
    today: str,
    for_github: bool,
    github_link: str,
) -> List[str]:
    compare = f"{github_link}/compare/{prev_tag}...{cur_tag}"
    lines = []
    if not for_github:
        lines = [
            f"## [{cur_tag[1:]}]({compare}) ({today})",
            "",
        ]

    breaking: List[str] = []

    for section in TYPES:
        try:
            type_section = log[section.key]
        except KeyError:
            continue

        show_breaking = section.key != BREAKING_CHANGE

        lines.extend([f"### {section.header}", ""])
        lines.extend(_show_links(github_link, type_section, show_breaking, for_github))
        breaking.extend(_find_breaking_notes(type_section))

    for section in sorted(log.keys()):
        if section in KNOWN_TYPES:
            continue
        type_section = log[section]
        try:
            section_header = ALL_TYPES[section]
        except KeyError:
            section_header = section

        lines.extend([f"### {section_header}", ""])
        lines.extend(_show_links(github_link, type_section, True, for_github))
        breaking.extend(_find_breaking_notes(type_section))

    if len(breaking):
        lines.extend([f"### BREAKING CHANGES", ""])
        lines.extend(breaking)

    if for_github:
        lines.append(f"**Full Changelog**: {compare}")

    return lines


def read_tag_date(tag: str):
    proc = capture("git", "log", "-n1", "--format=%aI", tag)
    if proc.returncode != 0:
        return time.strftime("%Y-%m-%d")
    return proc.stdout.decode("UTF-8").split("T", 1)[0]


def update_changelog(log: ChangeLog, cur_tag: str, prev_tag: str, github_link: str):
    lines = format_changelog(
        log, cur_tag, prev_tag, read_tag_date(cur_tag), False, github_link
    )

    with open(os.path.join(ROOT, "CHANGELOG.md")) as f:
        current = f.read().split("\n## ", 1)
    new_text = current[0] + "\n" + "\n".join(lines)
    if len(current) > 1:
        new_text += "\n## " + current[1]
    with open(os.path.join(ROOT, "CHANGELOG.md"), "wb") as f:
        f.write(new_text.encode("UTF-8"))
