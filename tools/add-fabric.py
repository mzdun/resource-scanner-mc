#!/usr/bin/env python3
# Copyright (c) 2024 Marcin Zdun
# This code is licensed under MIT license (see LICENSE for details)

import sys
import os
import shlex
import subprocess
import xml.etree.ElementTree as ET

from cache import Cache

__root__ = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

cache = Cache(os.path.join(__root__, "tools", ".fabric-cache"))


def fabric(path: str):
    return cache.json(f'https://meta.fabricmc.net/v2/versions/{path}')

def modrinth(endpoint):
    return cache.json(f'https://api.modrinth.com/v2{endpoint}')


def getFirst(flt, data):
    for item in filter(flt, data):
        return item
    return None


def getFirstStable(data):
    return getFirst(
        lambda item: item.get("stable") == True,
        data
    )


def gameVersionForApi(version: str) -> str:
    if version == "1.18":
        return "0.44.0+1.18"
    gameVersion = version.split('-', 1)[0]

    for ver in [
        ["22w13oneblockatatime"],
        ["24w", "1.21.2"],
        ["23w", "1.20.5"],
        ["22w", "1.19.3"],
        ["21w", "1.18"],
        ["20w", "1.17"],
        ["19w", "1.14"],
        ["18w", "1.14"]
    ]:
        prefix, branchName = ver if len(ver) == 2 else [ver[0], ver[0]]
        if gameVersion.startswith(prefix):
            return branchName

    return gameVersion.split('_', 1)[0]


def any(ign): return True


def prerelease(api: str):
    curr = [int(v) for v in api.split('.')]
    prev = [v for v in curr]
    index = len(prev) - 1
    while index > -1:
        if prev[index] > 0:
            prev[index] -= 1
            break
        index -= 1

    prevApi = '.'.join(str(v) for v in prev)
    return f'>{prevApi}'


def __main__():
    version = sys.argv[1]

    game = fabric('game')
    stableGame = getFirstStable(game)
    game = stableGame if version == '--stable' \
        else getFirst(any, game) if version == '--latest' \
        else getFirst(
            lambda item: item.get('version') == version,
            game
    )

    if game is None or 'version' not in game:
        print(f'cannot find Minecraft version for {version}')
        exit(1)

    version = game['version']
    apiVersion = gameVersionForApi(version)
    
    modmenu_versions = modrinth('/project/mOgUt4GM/version')
    matching = list(filter(lambda ver: version in ver['game_versions'], modmenu_versions))
    if len(matching) == 0:
        stableGameVersion = stableGame['version']
        matching = list(filter(lambda ver: stableGameVersion in ver['game_versions'], modmenu_versions))
    matching.sort(key=lambda v: v['date_published'], reverse=True)
    modmenu_version = matching[0]['version_number']

    def validApiVersion(api: str) -> bool:
        return api.endswith(f'-{apiVersion}') or api.endswith(f'+{apiVersion}')

    allGameYarn = list(filter(
        lambda item: item.get('gameVersion') == version,
        fabric('yarn')
    ))

    stableYarn = getFirstStable(allGameYarn)
    firstYarn = getFirst(any, allGameYarn)
    yarn = firstYarn if stableYarn is None or 'version' not in stableYarn else stableYarn
    if yarn is None or 'version' not in yarn:
        print(f'cannot find Yarn for Minecraft version {version}')
        exit(1)

    loader = getFirstStable(fabric('loader'))
    if loader is None or 'version' not in loader:
        print(f'cannot find a stable loader')
        exit(1)

    maven = cache.load(
        'https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/maven-metadata.xml')
    maven = ET.fromstring(maven).iter('version')
    maven = map(lambda e: e.text, maven)
    maven = filter(validApiVersion, maven)
    maven = list(maven)
    fabricAPI = maven[-1] if len(maven) else None
    if fabricAPI is None:
        print(f'cannot find Fabric API for Minecraft version {version}')
        exit(1)

    minecraft_dependency = ''
    if version != apiVersion:
        minecraft_dependency = f'\nminecraft_dependency={
            prerelease(apiVersion)}'

    template = f"""# Fabric Properties
# check these on https://fabricmc.net/develop
minecraft_version={game['version']}{minecraft_dependency}
yarn_mappings={yarn['version']}
loader_version={loader['version']}
modmenu_version={modmenu_version}

# Fabric API
fabric_version={fabricAPI}
"""
    print(template)

    dirname = os.path.join(__root__, 'fabric', version)
    if os.path.isdir(dirname):
        print(f'Directory {dirname} already exists')
        return

    os.makedirs(dirname, exist_ok=True)
    os.chdir(dirname)
    os.symlink(os.path.join('..', 'any', 'src'),
               'src', target_is_directory=True)
    os.symlink(os.path.join(
        '..', 'any', 'build.gradle'), 'build.gradle')
    with open('gradle.properties', 'w', encoding='UTF-8') as properties:
        print(template, file=properties)

    with open(os.path.join(__root__, 'settings.gradle'), 'a', encoding='UTF-8') as settings:
        print(f'include "fabric:{version}"', file=settings)

    print('Now visit https://fabricmc.net/develop/')

    propsArg = shlex.quote(os.path.join(dirname, 'gradle.properties'))
    if os.environ.get('TERM_PROGRAM') == 'vscode':
        print(f'code {propsArg}')
        subprocess.run(['code', os.path.join(
            dirname, 'gradle.properties')], shell=True)


if __name__ == '__main__':
    sys.exit(__main__())
