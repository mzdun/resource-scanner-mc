# Copyright (c) 2024 Marcin Zdun
# This code is licensed under MIT license (see LICENSE for details)

"Uploads mods to Modrinth"

import argparse
import os
from pprint import pprint
import re
import subprocess
import sys
from typing import Optional
from urllib.parse import urljoin, urlparse

from .api import MinotaurAPI
from .endpoints import ModifyProject
from .upload import Dep, Loader, enumArchives
from ..common.changelog import release_changelog
from ..common.project import Project, getVersion
from ..common.requests import RestResponse
from ..common.runner import Environment
from ..common.utils import PROJECT_ROOT, get_prog
from ..github.api import API

projectId = 'XTUTy4U4'
fabricAPIId = 'P7dR8mSH'
modMenuId = 'mOgUt4GM'

REQUIRED_DEP = 'required'
OPTIONAL_DEP = 'optional'

API_KEY = os.getenv('MODRINTH_TOKEN')

supportedLoaders = {
    'fabric': Loader('fabric', 'FB', [Dep(fabricAPIId, REQUIRED_DEP)])
}

exit_code = 0


def _print_response(response: RestResponse):
    global exit_code

    if response is None:
        return

    if response.status > 399:
        exit_code = 1

    if response.json is not None:
        description = response.json.get('description')
        error = response.json.get('error')
        if description is not None and error is not None:
            exit_code = 1
            print(f"{description} ({response.status} {response.reason})")
            return

    print(response.status, response.reason)
    print()
    if response.json is not None:
        pprint(response.json)
    elif response.text is not None:
        print(response.text)
    else:
        print(response.data)


def _readme(tag: str, project: Project):
    with open(os.path.join(PROJECT_ROOT, "README.md"), "rb") as readmeFile:
        readmeBytes = readmeFile.read()

    readmeBytes = readmeBytes.replace(b'\r\n', b'\n')
    readmeText = readmeBytes.decode('UTF-8')

    exclusions = readmeText.split('<!-- modrinth_exclude.start -->')
    chunks = [exclusions[0]]
    for exclusion in exclusions[1:]:
        saved = exclusion.split('<!-- modrinth_exclude.end -->', 1)[1]
        chunks.append(saved)

    readmeText = ''.join(chunks)
    readmeText = "\n".join(re.compile(r"[ \t]+\n").split(readmeText))
    readmeText = "\n\n".join(re.compile(r"\n\n\n+").split(readmeText))

    if project.github is None:
        return readmeText

    tagUrl = f'{project.github.url}/raw/{tag}/'
    matcher = re.compile(r'!\[[^]]+\]\(([^)]+)\)')
    pos = 0
    m = matcher.search(readmeText, pos)
    while m:
        oldSrc = m.group(1)
        newUrl = urljoin(tagUrl, oldSrc)
        if oldSrc == newUrl:
            pos = m.end()
        else:
            readmeText = \
                readmeText[:m.start(1)] + \
                newUrl + \
                readmeText[m.end(1):]
            pos = m.start(1) + len(newUrl)
        m = matcher.search(readmeText, pos)

    return readmeText


def upload(src: str):
    project = getVersion()
    archives = enumArchives(src, project, supportedLoaders,
                            Dep(modMenuId, OPTIONAL_DEP))
    api = MinotaurAPI(apiKey=API_KEY)

    changelog = release_changelog(str(project.version))
    for arch in archives:
        endpoint = arch.endpoint(projectId, changelog)
        response = endpoint.request(api)
        if response is not None and response.status < 400:
            if response.json is not None:
                version = response.json.get("id")
                url = f"https://modrinth.com/mod/{projectId}/version/{version}"
                suffixes = ''.join(
                    f'-{loader.name}' for loader in arch.loaders)
                minecraftVersion = f"{arch.minecraftVersion}{suffixes}"
                msg = f">>> You may now visit {minecraftVersion} at {url}"

                if version is not None:
                    print(msg)
        else:
            _print_response(response)

    endpoint = ModifyProject(
        id=projectId, body=_readme(project.tagName, project))
    response = endpoint.request(api)
    if response is not None and response.status > 399:
        _print_response(response)


parser = argparse.ArgumentParser(
    description=__doc__,
    prog=get_prog(__name__))

commands = parser.add_subparsers(required=True, dest='command')
send = commands.add_parser('send', help=__doc__, description=__doc__)
readme = commands.add_parser('readme', help="prints out parsed README.md", description="prints out parsed README.md")

Environment.addArgumentsTo(send)
Environment.addArgumentsTo(readme)

send.add_argument(
    "upload",
    metavar="<dir>",
    nargs=1,
    type=str,
    help="location of the JAR files to upload")

readme.add_argument(
    "tagname",
    metavar="<tag>",
    nargs=1,
    type=str,
    help="name of the tag this README taken from")


def __main__():
    args = parser.parse_args()
    Environment.apply(args)

    try:
        if args.command == 'send':
            upload(args.upload[0])
        elif args.command == 'readme':
            print(_readme(args.tagname[0], getVersion()))
    except subprocess.CalledProcessError as e:
        if e.stdout:
            print(e.stdout.decode("utf-8"), file=sys.stdout)
        if e.stderr:
            print(e.stderr.decode("utf-8"), file=sys.stderr)
        sys.exit(1)
    sys.exit(exit_code)


__main__()
