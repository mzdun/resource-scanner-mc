# Copyright (c) 2024 Marcin Zdun
# This code is licensed under MIT license (see LICENSE for details)

import email
import http.client
import json
import os
import time
import uuid

from dataclasses import dataclass
from typing import Dict, List, Optional, Union
from urllib.parse import urlparse, ParseResult


@dataclass
class CacheItem:
    uri: str
    path: str
    charset: Optional[str]
    modified: Optional[str]
    expires: Optional[float]

    def get_charset(self):
        return self.charset if self.charset is not None else 'UTF-8'

    def load(self, dirName: str, binary: bool = False):
        try:
            with open(os.path.join(dirName, "items", self.path), "rb") as input:
                data: bytes = input.read()
            return data if binary else data.decode(self.get_charset())
        except BaseException:
            return None

    def dump(self):
        result = {
            "uri": self.uri,
            "path": self.path,
        }
        if self.charset is not None:
            result["charset"] = self.charset
        if self.modified is not None:
            result["modified"] = self.modified
        if self.expires is not None:
            result["expires"] = self.expires
        return result

    def expired(self):
        if self.expires is None:
            return False
        return self.expires < time.time()


class Cache:
    dirName: str
    items: List[CacheItem] = []
    refs: Dict[str, CacheItem] = {}
    conn: Optional[http.client.HTTPSConnection] = None
    netloc: str = ''

    def __init__(self, dirName: str):
        self.dirName = dirName
        self.init()

    def init(self):
        index = os.path.join(self.dirName, 'index.json')
        try:
            with open(index) as indexData:
                data: List[Dict] = json.load(indexData)
            for item in data:
                uri = item.get("uri")
                path = item.get("path")
                charset = item.get("charset")
                modified = item.get("modified")
                expires = item.get("expires")
                if uri is None or path is None:
                    continue

                item = CacheItem(uri, path, charset, modified, expires)
                prev = self.refs.get(item.uri)
                if prev:
                    self.items.remove(prev)
                self.refs[item.uri] = item
                self.items.append(item)
            self.items.sort(key=lambda item: item.uri)
        except BaseException:
            self.items = []
            self.refs = {}

    def load(self, uri: str, binary: bool = False):
        url = urlparse(uri)
        item = self.refs.get(url.geturl())
        if item is None:
            return self._load(url, binary)
        if item.expired():
            return self._load(url, binary, item)
        if item.modified is not None:
            return self._load(url, binary, item, {'If-Modified-Since': item.modified})
        return item.load(self.dirName, binary)

    def json(self, uri: str):
        data = self.load(uri)
        if data is None:
            return None
        return json.loads(data)

    def _load(self, url: ParseResult, binary: bool, prevItem: Optional[CacheItem] = None, headers: Dict[str, str] = {}) -> Union[str, bytes]:
        resource = url.path
        if url.query != '':
            resource = f'{resource}?{url.query}'

        if self.conn is None or url.netloc != self.netloc:
            self.conn = http.client.HTTPSConnection(url.netloc)
            self.netloc = url.netloc
        self.conn.request('GET', resource, headers=headers)
        response = self.conn.getresponse()

        # print(response.status, response.reason, url.geturl())
        if response.status > 199 and response.status < 300:
            return self._load2xx(url, response, binary, prevItem)

        response.read()

        if response.status < 400 and prevItem is not None:
            return prevItem.load(self.dirName, binary)

        return None

    def _load2xx(self, url: ParseResult, response: http.client.HTTPResponse, binary: bool, prevItem: Optional[CacheItem] = None):
        data = response.read()

        http_modified = response.headers.get('Last-Modified')
        http_expires = response.headers.get('Expires')

        expires: Optional[float] = None
        if http_expires is not None:
            dateTuple = email.utils.parsedate(http_expires.strip())
            if dateTuple is not None:
                expires = time.mktime(dateTuple)

        http_charset = response.headers.get_charset()
        if http_charset is None:
            content_type = response.headers.get(
                'Content-Type', '').split(';')[1:]
            content_type = [chunk.strip().split('=', 1)
                            for chunk in content_type]
            content_type = {key.strip().lower(): value.strip()
                            for key, value in content_type}
            http_charset = content_type.get('charset')

        item = CacheItem(url.geturl(), prevItem.path if prevItem is not None else self._randomPath(), charset=http_charset,
                         modified=http_modified, expires=expires)
        self._store(item, data)

        return item.load(self.dirName, binary)

    def _store(self, item: CacheItem, data: bytes):
        prev = self.refs.get(item.uri)
        if prev:
            self.items.remove(prev)
        self.refs[item.uri] = item
        self.items.append(item)
        self.items.sort(key=lambda item: item.uri)

        os.makedirs(os.path.dirname(os.path.join(
            self.dirName, 'items', item.path)), exist_ok=True)

        with open(os.path.join(self.dirName, 'items', item.path), "wb") as output:
            output.write(data)

        with open(os.path.join(self.dirName, "index.json"), "w", encoding="UTF-8") as output:
            items: List[dict] = [item.dump() for item in self.items]
            json.dump(items, output, indent=2)

    def _randomPath(self):
        while True:
            key = str(uuid.uuid4()).replace('-', '')
            path = os.path.join(self.dirName, "items", key)
            if os.path.exists(path):
                continue
            return key
