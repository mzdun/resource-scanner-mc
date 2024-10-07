# Copyright (c) 2024 Marcin Zdun
# This code is licensed under MIT license (see LICENSE for details)

"Prepares a release draft and starts the build and publish process"

import argparse
import os
from typing import Optional

from ..common.changelog import FORCED_LEVEL, Level, format_commit_message, update_changelog
from ..common.git import addFiles, annotatedTag, bumpVersion, commit, getLog, getTags
from ..common.project import Project, getVersion, getVersionFilePath, setVersion
from ..common.runner import Environment, capture_str
from ..common.utils import PROJECT_ROOT
from ..github.api import API, format_release

SCOPE_FIX = {}

savedParser: argparse.ArgumentParser = None


def addReleaseArgumentsTo(parser: argparse.ArgumentParser):
    global savedParser
    savedParser = parser

    parser.add_argument(
        "--show-changelog",
        action="store_true",
        required=False,
        help="modifies CHANGELOG.md and gradle.properties even with --dry-run",
    )
    parser.add_argument(
        "--all",
        required=False,
        action="store_true",
        help="create a changelog with all sections, not only the 'feat', 'fix' and 'breaking'",
    )
    parser.add_argument(
        "--force",
        required=False,
        help="ignore the version change from changelog and instead use this settings' change",
        choices=FORCED_LEVEL.keys(),
    )
    parser.add_argument(
        "--stability",
        required=False,
        help="change the stability of the version",
    )
    parser.add_argument(
        "--use",
        required=False,
        help="ignore the version change from changelog and instead use the version from this param; cannot be used with --force or --stability",
    )


def runReleaseCommand(args: argparse.Namespace):
    level = FORCED_LEVEL.get(args.force)
    if args.use is not None and level is not None:
        savedParser.error("--use and --force cannot be used together")

    if args.use is not None and args.stability is not None:
        savedParser.error("--use and --force cannot be used together")

    current_branch = capture_str("git", "branch", "--show-current")

    if current_branch != "main" and not args.dry_run:
        color = "\033[1;31m" if args.color == "always" else ""
        reset = "\033[m" if args.color == "always" else ""
        print(f"""
{color}You are not on a main branch! Cowardly refusing to do any work.{reset}
""")
        exit(1)

    release(
        args.all,
        level,
        args.stability,
        args.show_changelog,
        args.use,
    )

    if current_branch != "main" and args.dry_run:
        color = "\033[1;31m" if args.color == "always" else ""
        reset = "\033[m" if args.color == "always" else ""
        print(f"""
{color}You are not on a main branch! On normal view this will not work.{reset}
""")

    if args.dry_run:
        color = "\033[1;32m" if args.color == "always" else ""
        reset = "\033[m" if args.color == "always" else ""
        print(f"""
{color}Have you updated docs/roadmap.md yet?{reset}
""")


def _nextVersion(project: Project, forced_level: Optional[Level], stability: Optional[str], level: Level):
    force_stability = False
    if forced_level is not None:
        force_stability = forced_level == Level.STABILITY
        level = forced_level if not force_stability else level

    next_stability = project.version.stability.value
    if stability is not None:
        stability.strip("-")
        if len(stability):
            stability = f"-{stability}"
        next_stability = stability
    if force_stability:
        if next_stability:
            stability_parts = next_stability.split('.')
            if len(stability_parts) == 1:
                next_stability = f"{next_stability}.2"
            else:
                iteration = int(stability_parts[1]) + 1
                stability_parts[1] = str(iteration)
                next_stability = '.'.join(stability_parts)
            level = Level.BENIGN
        else:
            next_stability = "-rc.1"
    else:
        if next_stability[:4] == '-rc.':
            next_stability = ''
            level = Level.BENIGN

    return f"v{bumpVersion(str(project.version), level)}{next_stability}"


def release(
    take_all: bool,
    forced_level: Optional[Level],
    stability: Optional[str],
    show_changelog: bool,
    use_version: Optional[str],
):
    project = getVersion()

    tags = getTags(project)
    log, level = getLog(tags, SCOPE_FIX, take_all)
    if use_version is not None:
        next_tag = use_version
        if next_tag[:1] != 'v':
            next_tag = f'v{next_tag}'
    else:
        next_tag = _nextVersion(project, forced_level, stability, level)

    if not Environment.DRY_RUN or show_changelog:
        update_changelog(log, next_tag, project.tagName, project.github.url)
        setVersion(next_tag[1:])

    commit_message = f"release {next_tag[1:]}"
    changelog = format_commit_message(log)

    addFiles(getVersionFilePath(),
             os.path.join(PROJECT_ROOT, "CHANGELOG.md"))
    commit(f"chore: {commit_message}{changelog}")
    annotatedTag(next_tag, commit_message)

    api = API.fromProject(project)

    if api.remote is not None:
        ghRelease = format_release(
            log, next_tag, project.tagName, api.url)
        htmlUrl = api.release(ghRelease).get("html_url")
        if htmlUrl is not None:
            print(f"Visit draft at {htmlUrl}")
