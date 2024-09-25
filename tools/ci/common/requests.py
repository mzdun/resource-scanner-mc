# Copyright (c) 2024 Marcin Zdun
# This code is licensed under MIT license (see LICENSE for details)

from abc import ABC, abstractmethod
import http.client
import json
import os
from typing import Any, BinaryIO, Dict, List, NamedTuple, Optional
from urllib.parse import ParseResult, urlparse
import uuid


DASHDASH = b'--'
CRLF = b'\r\n'
COLONSPACE = b': '


class Sink(ABC):
    @abstractmethod
    def write(self, data: bytes) -> "Sink":
        return self

    def writeUtf8(self, s: str) -> "Sink":
        return self.write(s.encode('UTF-8'))

    def writeInt(self, i: str) -> "Sink":
        return self.write(str(i).encode('UTF-8'))


class CountingSink(Sink):
    count: int

    def __init__(self):
        self.count = 0

    def write(self, data: bytes) -> Sink:
        self.count += len(data)
        return self


class BufferSink(Sink):
    chunks: List[bytes]

    def __init__(self):
        self.chunks = []

    def write(self, data: bytes) -> Sink:
        self.chunks.append(data)
        return self


class ProxySink(Sink):
    file: BinaryIO

    def __init__(self, file: BinaryIO):
        self.file = file

    def write(self, data: bytes) -> Sink:
        self.file.write(data)
        return self


class RequestBody(ABC):
    @abstractmethod
    def contentType(self) -> Optional[str]: pass
    @abstractmethod
    def contentLength(self) -> int: pass

    @abstractmethod
    def writeTo(self, sink: Sink) -> None: pass


class BytesRequest(RequestBody):
    def __init__(self, type: Optional[str], data: bytes):
        self.type = type
        self.data = data

    def contentType(self) -> Optional[str]:
        return self.type

    def contentLength(self) -> int:
        return len(self.data)

    def writeTo(self, sink: Sink):
        sink.write(self.data)


class Part(NamedTuple):
    headers: Dict[str, str]
    body: RequestBody


def _quotedString(value: str):
    value = value.replace('\n', '%0A')
    value = value.replace('\r', '%0D')
    value = value.replace('"', '%22')
    return f'"{value}"'


def _strToRequest(payload: str, contentType: Optional[str] = None):
    charset = 'UTF-8'
    data = payload.encode(charset)

    if contentType is not None:
        contentType = f'{contentType}; charset={charset}'

    return BytesRequest(contentType, data)


def _pathToRequest(path: str, contentType: Optional[str] = None):
    with open(path, "rb") as inFile:
        data = inFile.read()

    return BytesRequest(contentType, data)


def _createFormData(name: str, filename: Optional[str], body: RequestBody):
    disposition = f'form-data; name={_quotedString(name)}'
    if filename is not None:
        disposition = f'{disposition}; filename={_quotedString(filename)}'

    return Part({'Content-Disposition': disposition}, body)


class DynRequestBody(RequestBody):
    _contentLength: int = -1

    def __init__(self):
        super().__init__()

    def contentLength(self) -> int:
        result = self._contentLength
        if result < 0:
            sink = CountingSink()
            self.writeOrCountLength(sink, True)
            result = sink.count
            self._contentLength = result
        return result

    def writeTo(self, sink: Sink):
        self.writeOrCountLength(sink, False)

    @abstractmethod
    def writeOrCountLength(self, sink: Sink, isCounting: bool):
        pass


class Multipart(DynRequestBody):
    boundary: bytes
    type: str
    parts: List[Part]

    def __init__(self, type: str = "multipart/mixed"):
        super().__init__()
        self.boundary = str(uuid.uuid4()).encode('UTF-8')
        self.type = type
        self.parts = []

    def contentType(self) -> str:
        return f"{self.type}; boundary={self.boundary.decode('UTF-8')}"

    def writeOrCountLength(self, sink: Sink, counting: bool):
        for part in self.parts:
            sink.write(DASHDASH).write(self.boundary).write(CRLF)

            for header, value in part.headers.items():
                sink.writeUtf8(header).write(
                    COLONSPACE).writeUtf8(value).write(CRLF)

            contentType = part.body.contentType()
            contentLength = part.body.contentLength()

            if contentType is not None:
                sink.writeUtf8(
                    'Content-Type: ').writeUtf8(contentType).write(CRLF)

            if contentLength >= 0:
                sink.writeUtf8(
                    'Content-Length: ').writeInt(contentLength).write(CRLF)
            elif counting:
                sink.count = -1
                return

            sink.write(CRLF)
            if counting:
                sink.count += contentLength
            else:
                part.body.writeTo(sink)
            sink.write(CRLF)

        sink.write(DASHDASH).write(self.boundary).write(DASHDASH).write(CRLF)

    def addString(self, name: str, contents: str, mimeType: Optional[str] = None):
        self.parts.append(_createFormData(
            name, None, _strToRequest(contents, mimeType)))

    def addFile(self, name: str, filename: str, mimeType: Optional[str] = None):
        self.parts.append(_createFormData(
            name, os.path.basename(filename), _pathToRequest(filename, mimeType)))


class Request(ABC):
    def getMethod():
        return "GET"

    def getEndpoint() -> str:
        return '/'

    def getBody() -> Optional[RequestBody]:
        return None

    def getDefaultCharset():
        return "UTF-8"


class Request(NamedTuple):
    method: str = "GET"
    endpoint: str = "/"
    body: Optional[RequestBody] = None
    defaultCharset: str = "UTF-8"


class RestResponse(NamedTuple):
    status: int
    reason: str
    mediaType: Optional[str]
    charset: str
    data: bytes
    text: str
    json: Any

    def is1xx(self):
        return self.status < 200

    def is2xx(self):
        return self.status > 199 and self.status < 300

    def is3xx(self):
        return self.status > 299 and self.status < 400

    def isError(self):
        return self.status > 399


class RestAPI:
    baseUrl: str
    conn: Optional[http.client.HTTPSConnection] = None
    netloc: Optional[str]

    def __init__(self, baseUrl: str):
        self.baseUrl = baseUrl

    def request(self, req: Request):
        url = urlparse(self.baseUrl + req.endpoint)
        resource = url.path
        if url.query != '':
            resource = f'{resource}?{url.query}'

        if self.conn is None or url.netloc != self.netloc:
            self.conn = http.client.HTTPSConnection(url.netloc)
            self.netloc = url.netloc

        headers = self.getHeaders(req, url)
        body = req.body

        if body is not None:
            contentType = body.contentType()
            contentLength = body.contentLength()

            if contentType is not None:
                headers['Content-Type'] = contentType

            if contentLength >= 0:
                headers['Content-Length'] = contentLength

            sink = BufferSink()
            body.writeTo(sink)

            self.conn.request(req.method, resource,
                              body=sink.chunks, headers=headers)
        else:
            self.conn.request(req.method, resource, headers=headers)

        response = self.conn.getresponse()

        mediaType = response.headers.get_content_type()
        charset = response.headers.get_charset()
        if charset is None:
            contentType = response.headers.get(
                'Content-Type', '').split(';')[1:]
            contentType = [chunk.strip().split('=', 1)
                           for chunk in contentType]
            contentType = {key.strip().lower(): value.strip()
                           for key, value in contentType}
            charset = contentType.get('charset')

        if charset is None:
            if mediaType == "application/json":
                charset = 'UTF-8'
            else:
                charset = req.defaultCharset

        data = response.read()
        text = data.decode(charset)
        jsonData = json.loads(
            text) if mediaType == "application/json" else None

        return RestResponse(status=response.status,
                            reason=response.reason,
                            mediaType=mediaType,
                            charset=charset,
                            data=data,
                            text=text,
                            json=jsonData)

    def getHeaders(self, req: Request, url: ParseResult) -> Dict[str, str]:
        return {}
