# Copyright (c) 2024 Marcin Zdun
# This code is licensed under MIT license (see LICENSE for details)


import os
from pprint import pprint
import re
import time
from enum import Enum
from typing import Dict, List, NamedTuple

from .runner import capture
from .utils import PROJECT_ROOT


def release_changelog(version: str):
    h2_prefix = '## ['
    h2_current_prefix = f'## [{version}]'
    collecting = False
    lines = []
    with open(os.path.join(PROJECT_ROOT, "CHANGELOG.md"), encoding="UTF-8") as changelog_file:
        for line in changelog_file.readlines():
            line = line.rstrip()
            is_h2 = line[:len(h2_prefix)] == h2_prefix
            if not is_h2 and collecting:
                if line[:1] == '#':
                    line = line.lstrip('# \t').strip()
                    line = f'**{line}**'
                lines.append(line)
                continue
            collecting = line[:len(h2_current_prefix)] == h2_current_prefix

    text = "\n".join(lines).strip()
    if text == "":
        text = None

    return text


class Section(NamedTuple):
    key: str
    header: str


class Level(Enum):
    BENIGN = 0
    STABILITY = 1
    PATCH = 2
    FEATURE = 3
    BREAKING = 4


FORCED_LEVEL: Dict[str, Level] = {
    "patch": Level.PATCH,
    "fix": Level.PATCH,
    "minor": Level.FEATURE,
    "feat": Level.FEATURE,
    "feature": Level.FEATURE,
    "major": Level.BREAKING,
    "breaking": Level.BREAKING,
    "release": Level.BREAKING,
    "stability": Level.STABILITY,
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


class ChangelogMessage:
    def intro_lines(self) -> List[str]:
        return []

    def section_header(self, lines: List[str], header) -> List[str]:
        lines.extend([f"### {header}", ""])

    def issue_link(self, ref: str) -> str:
        return ref

    def scope_text(self, scope: str) -> str:
        if len(scope):
            scope = f"{scope}: "
        return scope

    def short_hash_link(self, link: CommitLink) -> str:
        return link.short_hash

    def list_item(self, lineMarkup: str) -> str:
        return f"- {lineMarkup}"

    def outro_lines(self, _: List[str]) -> None:
        pass

    def post_process(self, lines: List[str]):
        return "\n".join(lines)

    def link_str(self, link: CommitLink, show_breaking: bool):
        scope = ""
        if len(link.scope):
            scope = link.scope

        if show_breaking:
            if link.is_breaking:
                if len(scope):
                    scope = f"breaking, {scope}"
                else:
                    scope = f"breaking"
        scope = self.scope_text(scope)

        refs = ""
        for refs_name in link.references:
            refs_links = [
                self.issue_link(issue) for issue in link.references[refs_name] if issue != ""
            ]
            if len(refs_links) > 0:
                refs += f", {refs_name} "
                if len(refs_links) == 1:
                    refs += refs_links[0]
                else:
                    last = refs_links[-1]
                    listed = ", ".join(refs_links[:-1])
                    refs += f"{listed} and {last}"

        return self.list_item(f"{scope}{link.summary} ({self.short_hash_link(link)}){refs}")

    def show_links(self, links: List[CommitLink], show_breaking: bool) -> List[str]:
        issues: Dict[str, List[str]] = {}
        for link in links:
            scope = link.scope
            if scope not in issues:
                issues[scope] = []
            issues[scope].append(self.link_str(link, show_breaking))
        result = []
        for scope in sorted(issues.keys()):
            result.extend(issues[scope])
        if len(result):
            result.append("")
        return result

    def format_changelog(self, log: ChangeLog) -> List[str]:
        lines = self.intro_lines()

        breaking: List[str] = []

        for section in TYPES:
            try:
                type_section = log[section.key]
            except KeyError:
                continue

            show_breaking = section.key != BREAKING_CHANGE

            self.section_header(lines, section.header)
            lines.extend(self.show_links(type_section, show_breaking))
            breaking.extend(_find_breaking_notes(type_section))

        for section in sorted(log.keys()):
            if section in KNOWN_TYPES:
                continue
            type_section = log[section]
            try:
                section_header = ALL_TYPES[section]
            except KeyError:
                section_header = section

            self.section_header(lines, section_header)
            lines.extend(self.show_links(type_section, True))
            breaking.extend(_find_breaking_notes(type_section))

        if len(breaking):
            self.section_header(lines, "BREAKING CHANGES")
            lines.extend(breaking)

        self.outro_lines(lines)

        return self.post_process(lines)


class GithubReleaseChangelog(ChangelogMessage):
    github_link: str
    cur_tag: str
    prev_tag: str

    def __init__(self, github_link: str, cur_tag: str, prev_tag: str):
        self.github_link = github_link
        self.cur_tag = cur_tag
        self.prev_tag = prev_tag

    def scope_text(self, scope: str):
        if len(scope):
            scope = f"**{scope}**: "
        return scope

    def short_hash_link(self, link: CommitLink):
        return f"[{link.short_hash}]({self.github_link}/commit/{link.hash})"

    def outro_lines(self, lines: List[str]) -> None:
        range = f"{self.prev_tag}...{self.cur_tag}"
        compare = f"{self.github_link}/compare/{range}"
        lines.append(f"**Full Changelog**: {compare}")


class ChangelogFileUpdate(GithubReleaseChangelog):
    commit_date: str

    def __init__(self, github_link: str, cur_tag: str, prev_tag: str, commit_date: str):
        super().__init__(github_link, cur_tag, prev_tag)
        self.commit_date = commit_date

    def intro_lines(self) -> List[str]:
        range = f"{self.prev_tag}...{self.cur_tag}"
        compare = f"{self.github_link}/compare/{range}"
        return [
            f"## [{self.cur_tag[1:]}]({compare}) ({self.commit_date})"
            "",
        ]

    def outro_lines(self, _: List[str]) -> None:
        pass

    def issue_link(self, ref: str):
        if ref[:1] == "#" and ref[1:].isdigit():
            return f"[{ref}]({self.github_link}/issues/{ref[1:]})"
        return ref


class ReleaseCommitMessage(ChangelogMessage):
    def section_header(self, lines: List[str], header) -> List[str]:
        lines.extend([f"{header}:", ""])

    def list_item(self, lineMarkup: str):
        return f" - {lineMarkup}"

    def post_process(self, lines: List[str]):
        paras = "\n".join(lines).strip().split("\n\n")

        text = "\n\n".join(ReleaseCommitMessage._wrapAt78(para)
                           for para in paras)
        if len(text):
            text = f"\n\n{text}"
        return text

    @staticmethod
    def _wrapAt78(para: str):
        if para[:3] == ' - ':
            lines = para.split('\n')
            lines = [ReleaseCommitMessage._wrapAt(
                75, line[3:], " - ", "   ") for line in lines]
            return '\n'.join(lines)
        return ReleaseCommitMessage._wrapAt(78, para, "", "")

    @staticmethod
    def _wrapAt(length: int, para: str, firstLine: str, nextLines: str):
        result = ""
        line = firstLine
        lineIsDirty = False
        words = para.strip().split(' ')
        for word in words:
            wordLen = len(word)
            if wordLen == 0:
                continue

            lineIsDirty = True
            lineLen = len(line)
            space = ' ' if lineLen > 0 and line[-1] != ' ' else ''
            resultingLen = lineLen + len(space) + wordLen
            if resultingLen <= length:
                line = f'{line}{space}{word}'
                continue
            result = f'{result}{line}\n'
            line = f'{nextLines}{word}'

        if lineIsDirty:
            result = f'{result}{line}'
        return result


def _find_breaking_notes(links: List[CommitLink]) -> List[str]:
    breaking: List[str] = []
    for link in links:
        if link.breaking_message is None:
            continue
        for para in link.breaking_message:
            text = re.sub(r"\s+", " ", para.strip())
            if text != "":
                breaking.append(text + "\n")
    return breaking


def read_tag_date(tag: str):
    proc = capture("git", "log", "-n1", "--format=%aI", tag)
    if proc.returncode != 0:
        return time.strftime("%Y-%m-%d")
    return proc.stdout.decode("UTF-8").split("T", 1)[0]


def update_changelog(log: ChangeLog, cur_tag: str, prev_tag: str, github_link: str):
    update = ChangelogFileUpdate(
        github_link, cur_tag, prev_tag, read_tag_date(cur_tag))

    with open(os.path.join(PROJECT_ROOT, "CHANGELOG.md")) as f:
        current = f.read().split("\n## ", 1)
    new_text = current[0] + "\n" + \
        update.format_changelog(log)
    if len(current) > 1:
        new_text += "\n## " + current[1]
    with open(os.path.join(PROJECT_ROOT, "CHANGELOG.md"), "wb") as f:
        f.write(new_text.encode("UTF-8"))


def format_commit_message(log: ChangeLog):
    return ReleaseCommitMessage().format_changelog(log)
