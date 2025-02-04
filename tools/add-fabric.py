#!/usr/bin/env python3
# Copyright (c) 2024 Marcin Zdun
# This code is licensed under MIT license (see LICENSE for details)

import argparse
import os
import shlex
import subprocess
import sys
import xml.etree.ElementTree as ET
from typing import Callable, List, cast

from cache import Cache

__root__ = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

cache = Cache(os.path.join(__root__, "tools", ".fabric-cache"))

Objects = List[dict]


def fabric(path: str):
    return cache.json(f"https://meta.fabricmc.net/v2/versions/{path}") or []


def modrinth(endpoint):
    return cache.json(f"https://api.modrinth.com/v2{endpoint}") or []


def getFirst(flt: Callable[[dict], bool], data: Objects):
    try:
        return next(filter(flt, data))
    except StopIteration:
        return None


def getFirstStable(data: Objects):
    return getFirst(lambda item: item.get("stable") == True, data)


def gameVersionForApi(version: str) -> str:
    if version == "1.18":
        return "0.44.0+1.18"
    gameVersion = version.split("-", 1)[0]

    for ver in [
        ["22w13oneblockatatime"],
        ["25w", "1.21.5"],
        ["24w", "1.21.2"],
        ["23w", "1.20.5"],
        ["22w", "1.19.3"],
        ["21w", "1.18"],
        ["20w", "1.17"],
        ["19w", "1.14"],
        ["18w", "1.14"],
    ]:
        prefix, branchName = ver if len(ver) == 2 else [ver[0], ver[0]]
        if gameVersion.startswith(prefix):
            return branchName

    return gameVersion.split("_", 1)[0]


def any(ign):
    return True


def prerelease(api: str):
    curr = [int(v) for v in api.split(".")]
    prev = [v for v in curr]
    index = len(prev) - 1
    while index > -1:
        if prev[index] > 0:
            prev[index] -= 1
            break
        index -= 1

    prevApi = ".".join(str(v) for v in prev)
    return f">{prevApi}"


def __main__():
    parser = argparse.ArgumentParser(
        description="Adds reference to a fabric platform", add_help=False
    )
    parser.add_argument(
        "-h",
        "--help",
        action="help",
        default=argparse.SUPPRESS,
        help="Show this help message and exit",
    )
    version_group = parser.add_mutually_exclusive_group(required=True)
    version_group.add_argument(
        metavar="fabric-version",
        dest="fabric_version",
        nargs="?",
        help="Add this exact value of fabric version",
    )
    version_group.add_argument(
        "--stable",
        action="store_true",
        default=False,
        help='Add latest known version marked "stable"',
    )
    version_group.add_argument(
        "--latest",
        action="store_true",
        default=False,
        help="Add latest known version of fabric",
    )
    version_group.add_argument(
        "--list",
        action="store_true",
        default=False,
        help="Print 10 latest versions of fabric, 5 stable, 5 unstable",
    )
    args = parser.parse_args()
    print(args)

    games = fabric("game")
    if not games:
        print("There are no fabric version available.", file=sys.stderr)
        return 1

    if args.list:
        stable_versions: List[str] = []
        unstable_versions: List[str] = []

        for info in games:
            if len(stable_versions) == 5 and len(unstable_versions) == 5:
                break
            version = cast(str, info.get("version", "?"))
            stable = cast(bool, info.get("stable", False))

            check = stable_versions if stable else unstable_versions
            if len(check) == 5:
                continue

            check.append(version)
            print(f"{version} ({'' if stable else 'un'}stable)")

        return 0

    stableGame = getFirstStable(games)
    version = args.fabric_version
    game = (
        stableGame
        if args.stable
        else (
            getFirst(any, games)
            if args.latest
            else getFirst(lambda item: item.get("version") == version, games)
        )
    )

    if game is None or "version" not in game:
        print(f"cannot find Minecraft version for {version}")
        exit(1)

    version = game["version"]
    apiVersion = gameVersionForApi(version)

    modmenu_versions = modrinth("/project/mOgUt4GM/version")
    matching = list(
        filter(lambda ver: version in ver["game_versions"], modmenu_versions)
    )
    modmenu_rt = "true"
    if len(matching) == 0:
        stableGameVersion = stableGame["version"] if stableGame else None
        matching = list(
            filter(
                lambda ver: stableGameVersion in ver["game_versions"], modmenu_versions
            )
        )
        modmenu_rt = "false"
    if len(matching) == 0:
        matching = modmenu_versions
        modmenu_rt = "false"
    matching.sort(key=lambda v: v["date_published"], reverse=True)
    modmenu_version = matching[0]["version_number"]

    def validApiVersion(api: str) -> bool:
        return api.endswith(f"-{apiVersion}") or api.endswith(f"+{apiVersion}")

    allGameYarn = list(
        filter(lambda item: item.get("gameVersion") == version, fabric("yarn"))
    )

    stableYarn = getFirstStable(allGameYarn)
    firstYarn = getFirst(any, allGameYarn)
    yarn = (
        firstYarn if stableYarn is None or "version" not in stableYarn else stableYarn
    )
    if yarn is None or "version" not in yarn:
        print(f"cannot find Yarn for Minecraft version {version}")
        exit(1)

    loader = getFirstStable(fabric("loader"))
    if loader is None or "version" not in loader:
        print(f"cannot find a stable loader")
        exit(1)

    maven = (
        cache.load(
            "https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/maven-metadata.xml"
        )
        or ""
    )
    maven = ET.fromstring(maven).iter("version")
    maven = map(lambda e: cast(str, e.text), maven)
    maven = filter(validApiVersion, maven)
    maven = list(maven)
    fabricAPI = maven[-1] if len(maven) else None
    if fabricAPI is None:
        print(f"cannot find Fabric API for Minecraft version {version}")
        exit(1)

    minecraft_dependency = ""
    if version != apiVersion:
        minecraft_dependency = f"\nminecraft_dependency={
            prerelease(apiVersion)}"

    template = f"""# Fabric Properties
# check these on https://fabricmc.net/develop
minecraft_version={game['version']}{minecraft_dependency}
yarn_mappings={yarn['version']}
loader_version={loader['version']}

# Dependencies
fabric_version={fabricAPI}
modmenu_version={modmenu_version}
modmenu_rt={modmenu_rt}
"""
    print(template)

    dirname = os.path.join(__root__, "fabric", version)
    if os.path.isdir(dirname):
        print(f"Directory {dirname} already exists")
        return

    def symlink(*subdirs: str):
        link = subdirs[-1]
        subdirs = subdirs[:-1]
        localDirname = os.path.join(dirname, *subdirs)
        target = os.path.join(*([".."] * len(subdirs)), "..", "1.21", *subdirs, link)
        os.makedirs(localDirname, exist_ok=True)
        os.chdir(localDirname)
        os.symlink(target, link, target_is_directory=True)

    symlink("src", "main", "java", "com")
    symlink("src", "main", "java", "api", "compat", "common")
    symlink("src", "main", "resources")
    symlink("src", "test")

    os.chdir(dirname)
    os.symlink(os.path.join("..", "1.21", "build.gradle"), "build.gradle")

    with open("gradle.properties", "w", encoding="UTF-8") as properties:
        properties.write(template)

    with open(
        os.path.join(__root__, "settings.gradle"), "a", encoding="UTF-8"
    ) as settings:
        print(f'include "fabric:{version}"', file=settings)

    print("Now visit https://fabricmc.net/develop/")

    propsArg = shlex.quote(os.path.join(dirname, "gradle.properties"))
    if os.environ.get("TERM_PROGRAM") == "vscode":
        print(f"code {propsArg}")
        subprocess.run(["code", os.path.join(dirname, "gradle.properties")], shell=True)


if __name__ == "__main__":
    sys.exit(__main__())
