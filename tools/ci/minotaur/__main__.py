# Copyright (c) 2024 Marcin Zdun
# This code is licensed under MIT license (see LICENSE for details)

"Uploads mods to Modrinth"

import argparse
import os
from pprint import pprint
import subprocess
import sys

from .api import MinotaurAPI
from .endpoints import ModifyProject
from .upload import Dep, Loader, enumArchives
from ..common.changelog import release_changelog
from ..common.project import getVersion
from ..common.requests import RestResponse
from ..common.runner import Environment
from ..common.utils import PROJECT_ROOT, get_prog

projectId = 'XTUTy4U4'
fabricAPIId = 'P7dR8mSH'

REQUIRED_DEP = 'required'

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


def _readme():
    with open(os.path.join(PROJECT_ROOT, "README.md"), "rb") as readmeFile:
        readmeBytes = readmeFile.read()

    readmeBytes = readmeBytes.replace(b'\r\n', b'\n')
    readme = readmeBytes.decode('UTF-8')

    exclusions = readme.split('<!-- modrinth_exclude.start -->')
    chunks = [exclusions[0]]
    for exclusion in exclusions[1:]:
        saved = exclusion.split('<!-- modrinth_exclude.end -->', 1)[1]
        chunks.append(saved.lstrip('\r\n'))

    return ''.join(chunks)


def upload(src: str):
    project = getVersion()
    archives = enumArchives(src, project, supportedLoaders)
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
                minecraft_version = f"{arch.minecraft_version}{suffixes}"
                msg = f">>> You may now visit {minecraft_version} at {url}"

                if version is not None:
                    print(msg)
        else:
            _print_response(response)

    endpoint = ModifyProject(id=projectId, body=_readme())
    response = endpoint.request(api)
    if response is not None and response.status > 399:
        _print_response(response)


parser = argparse.ArgumentParser(
    description=__doc__,
    prog=get_prog(__name__))

commands = parser.add_subparsers(required=True, dest='command')
send = commands.add_parser('send', help=__doc__, description=__doc__)

Environment.addArgumentsTo(send)

send.add_argument(
    "upload",
    metavar="<dir>",
    nargs=1,
    type=str,
    help="location of the JAR files to upload")


def __main__():
    args = parser.parse_args()
    Environment.apply(args)

    try:
        upload(args.upload[0])
    except subprocess.CalledProcessError as e:
        if e.stdout:
            print(e.stdout.decode("utf-8"), file=sys.stdout)
        if e.stderr:
            print(e.stderr.decode("utf-8"), file=sys.stderr)
        sys.exit(1)
    sys.exit(exit_code)


__main__()
