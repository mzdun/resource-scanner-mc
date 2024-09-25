# Copyright (c) 2024 Marcin Zdun
# This code is licensed under MIT license (see LICENSE for details)

from typing import Dict, Optional
from urllib.parse import ParseResult
from ..common.requests import Request, RestAPI


class MinotaurAPI(RestAPI):
    apiKey: Optional[str]

    def __init__(self, baseUrl: str = "https://api.modrinth.com/v2", apiKey: Optional[str] = None):
        super().__init__(baseUrl)
        self.apiKey = apiKey

    def getHeaders(self, req: Request, url: ParseResult) -> Dict[str, str]:
        headers: Dict[str, str] = {}

        if self.apiKey is not None:
            headers['Authorization'] = self.apiKey

        return headers
