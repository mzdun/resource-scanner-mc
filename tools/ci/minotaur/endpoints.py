# Copyright (c) 2024 Marcin Zdun
# This code is licensed under MIT license (see LICENSE for details)

from abc import ABC, abstractmethod
import json
import os
from pprint import pprint
from typing import Any, Dict, List, NamedTuple, Optional, Sequence

from ..common.requests import BytesRequest, Multipart, Request, RequestBody
from ..common.runner import Environment
from .api import MinotaurAPI


def _jsonOneLine(data):
    return json.dumps(data, ensure_ascii=True)


def _yamlSimple(data):
    if data is None:
        if Environment.USE_COLOR:
            print("\033[33;3mnull\033[m", end='')
        else:
            print("null", end='')
        return

    if isinstance(data, bool):
        if Environment.USE_COLOR:
            print(f"\033[33;3m{'true' if data else 'false'}\033[m", end='')
        else:
            print('true' if data else 'false', end='')
        return

    if isinstance(data, str):
        data = data.replace('\\', '\\\\')
        data = data.replace('\n', '\\n')
        data = data.replace('\r', '\\r')
        data = data.replace('\t', '\\t')
        if Environment.USE_COLOR:
            if len(data) > 100:
                data = f"\033[31;3m\"{data[:97]}\033[m\033[30;2m...\033[m"
            else:
                data = f"\033[31;3m\"{data}\"\033[m"
        elif len(data) > 100:
            data = f"\"{data[:97]}..."
        else:
            data = f"\"{data}\""
        print(data, end='')
        return

    print(data, end='')


def _yamlArray(data: Sequence, indent: int, forArray: bool = False):
    prefix = "  " * indent + "- "
    for sub in data:
        print(f"{prefix}", end='')
        if not isinstance(sub, (list, tuple, dict)):
            _yamlSimple(sub)
            print()
        if isinstance(sub, (list, tuple)):
            print()
            _yamlArray(sub, indent + 1, True)
        if isinstance(sub, dict):
            _yamlDictionary(sub, indent + 1, True)


def _yamlDictionary(data: dict[str, Any], indent: int, arrayItem: bool):
    prefix = "  " * indent
    for key in data:
        sub = data[key]
        linePrefix = "" if arrayItem else prefix
        arrayItem = False
        if Environment.USE_COLOR:
            print(f"{linePrefix}\033[32;3;2m{key}\033[0;3m:\033[m ", end='')
        else:
            print(f"{linePrefix}{key}: ", end='')
        if not isinstance(sub, (list, tuple, dict)):
            _yamlSimple(sub)
        print()
        if isinstance(sub, (list, tuple)):
            _yamlArray(sub, indent + 1, False)
        if isinstance(sub, dict):
            _yamlDictionary(sub, indent + 1, False)


def _wouldHave(endpoint: "Endpoint.Builder", api: MinotaurAPI, body: Any = None):
    req = endpoint.build()
    method = req.method
    url = f"{api.baseUrl}{req.endpoint}"
    if Environment.USE_COLOR:
        method = f'\033[33m{method}\033[m'
        url = f'\033[96m{url}\033[m'
    if body is None:
        print(f'Would {method} {url}')
        return
    print(f'Would {method} {url} with')
    # print(_jsonPretty(body))
    _yamlDictionary(body, 1, False)


def _request(endpoint: "Endpoint.Builder", api: MinotaurAPI):
    req = endpoint.build()
    url = f"{api.baseUrl}{req.endpoint}"
    if not Environment.USE_COLOR:
        print(f'{req.method} {url}')
    else:
        print(f'\033[33m{req.method}\033[m \033[96m{url}\033[m')
    return api.request(req)


class Endpoint(ABC):
    def getMethod(self) -> str:
        return "GET"

    @abstractmethod
    def getEndpoint(self) -> str:
        pass

    class Builder:
        ep: "Endpoint"
        kwargs: dict
        body: Optional[RequestBody] = None

        def __init__(self, ep: "Endpoint", kwargs: dict):
            self.ep = ep
            self.kwargs = kwargs

        def setBody(self, body: RequestBody):
            self.body = body

        def build(self):
            return Request(method=self.ep.getMethod(),
                           endpoint=self.ep.getEndpoint().format(**self.kwargs), body=self.body)

    def prepareRequest(self, **kwargs):
        return Endpoint.Builder(self, kwargs)


class ModifyProject(NamedTuple):
    id: str
    slug: Optional[str] = None
    title: Optional[str] = None
    description: Optional[str] = None
    body: Optional[str] = None

    class endpoint(Endpoint):
        def getMethod(self) -> str:
            return "PATCH"

        def getEndpoint(self) -> str:
            return "/project/{id}"

    def request(self, api: MinotaurAPI):
        req = ModifyProject.endpoint().prepareRequest(id=self.id)

        data = self.json()
        if Environment.DRY_RUN:
            _wouldHave(req, api, data)
            return None

        req.setBody(BytesRequest(type="application/json",
                                 data=_jsonOneLine(data).encode('UTF-8')))
        return _request(req, api)

    def json(self):
        data: Dict[str, str] = {}
        for field in ["slug", "title", "description", "body"]:
            value = getattr(self, field)
            if value is None:
                continue
            data[field] = value
        return data


class CreateVersion(NamedTuple):
    dirname: str
    basename: str
    name: str
    project_id: str
    version_number: str
    dependencies: list
    game_versions: List[str]
    version_type: str
    loaders: List[str]
    featured: bool

    changelog: Optional[str] = None

    class endpoint(Endpoint):
        def getMethod(self) -> str:
            return "POST"

        def getEndpoint(self) -> str:
            return "/version"

    def request(self, api: MinotaurAPI):
        req = CreateVersion.endpoint().prepareRequest()

        data = self.json()
        if Environment.DRY_RUN:
            _wouldHave(req, api, data)
            return None

        body = Multipart()
        body.addString("data", _jsonOneLine(data), "application/json")
        body.addFile(
            self.basename,
            os.path.join(self.dirname, self.basename),
            "application/java-archive")
        req.setBody(body)

        return _request(req, api)

    def json(self):
        result = {}

        for key in ["name", "project_id", "version_number", "dependencies", "game_versions",
                    "version_type", "loaders", "featured"]:
            result[key] = getattr(self, key)

        changelog = self.changelog
        if changelog is None or changelog == "":
            changelog = "No changelog was specified."
        result["changelog"] = changelog

        result["file_parts"] = [self.basename]

        return result
