#!/usr/bin/env python3
"""Deterministic upstreams used only by the ephemeral DAST environment."""

import argparse
import json
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import urlparse


class UpstreamHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        path = urlparse(self.path).path
        if path == "/search":
            self._json({"results": [{"latitude": -0.2201641, "longitude": -78.5123274}]})
            return
        if path == "/forecast":
            self._json({
                "hourly": {
                    "wind_speed_10m": [12.0],
                    "precipitation": [0.2],
                    "visibility": [10000.0],
                    "cloudcover": [35],
                }
            })
            return
        self.send_error(404)

    def _json(self, payload):
        body = json.dumps(payload).encode("utf-8")
        self.send_response(200)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def log_message(self, _format, *_args):
        return


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--port", type=int, default=19090)
    args = parser.parse_args()
    ThreadingHTTPServer(("0.0.0.0", args.port), UpstreamHandler).serve_forever()


if __name__ == "__main__":
    main()
