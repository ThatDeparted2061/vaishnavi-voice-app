#!/usr/bin/env python3
"""
Vaishnavi Voice App — Dev Backend Server
=========================================
A lightweight HTTP server that the Android app (and OpenClaw) talks to.

Endpoints
---------
  GET  /health         → {"status": "ok", "mode": "echo", "port": 9000}
  POST /api/message    → {"reply": "You said: <message>"}

Binds to 127.0.0.1 (localhost only) so it is not reachable from other
machines on the network.

Lifecycle
---------
  Start : python3 server.py
  Stop  : ./stop-server.sh
          — OR —
          kill $(cat .vaishnavi.pid)
          — OR —
          Ctrl+C in the terminal where it runs

The server writes its PID to .vaishnavi.pid on startup and removes it on
clean exit, so stop-server.sh can always find and terminate it reliably.
"""

import json
import os
import signal
import sys
import types
from http.server import BaseHTTPRequestHandler, HTTPServer
from typing import Optional

PORT = 9000
PID_FILE = os.path.join(os.path.dirname(os.path.abspath(__file__)), ".vaishnavi.pid")


# ---------------------------------------------------------------------------
# PID-file helpers
# ---------------------------------------------------------------------------

def write_pid() -> None:
    with open(PID_FILE, "w") as fh:
        fh.write(str(os.getpid()))


def remove_pid() -> None:
    try:
        os.remove(PID_FILE)
    except OSError:
        pass


# ---------------------------------------------------------------------------
# Signal handler — clean shutdown on SIGINT (Ctrl+C) and SIGTERM
# ---------------------------------------------------------------------------

def _shutdown(signum: int, frame: Optional[types.FrameType]) -> None:
    remove_pid()
    print("\n[vaishnavi] Server stopped. Goodbye.")
    sys.exit(0)


# ---------------------------------------------------------------------------
# Request handler
# ---------------------------------------------------------------------------

class VaishnaviHandler(BaseHTTPRequestHandler):
    """Handle GET /health and POST /api/message."""

    # Silence the default per-request log lines; errors are still printed.
    def log_message(self, fmt: str, *args: object) -> None:
        pass

    # --- helpers -----------------------------------------------------------

    def _send_json(self, code: int, body: dict) -> None:
        data = json.dumps(body).encode()
        self.send_response(code)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(data)))
        self.end_headers()
        self.wfile.write(data)

    # --- routes ------------------------------------------------------------

    def do_GET(self) -> None:
        if self.path == "/health":
            self._send_json(200, {
                "status": "ok",
                "mode": "echo",
                "port": PORT,
            })
        else:
            self._send_json(404, {"error": "not found"})

    def do_POST(self) -> None:
        if self.path == "/api/message":
            length = int(self.headers.get("Content-Length", 0))
            try:
                body = json.loads(self.rfile.read(length) or b"{}")
            except json.JSONDecodeError:
                self._send_json(400, {"error": "invalid JSON"})
                return
            message = body.get("message", "")
            self._send_json(200, {"reply": f"You said: {message}"})
        else:
            self._send_json(404, {"error": "not found"})


# ---------------------------------------------------------------------------
# Entry point
# ---------------------------------------------------------------------------

if __name__ == "__main__":
    signal.signal(signal.SIGTERM, _shutdown)
    signal.signal(signal.SIGINT, _shutdown)

    write_pid()

    print(f"[vaishnavi] Backend server started")
    print(f"[vaishnavi]   Health   : http://localhost:{PORT}/health")
    print(f"[vaishnavi]   Messages : http://localhost:{PORT}/api/message")
    print(f"[vaishnavi]   PID      : {os.getpid()} (saved to {PID_FILE})")
    print(f"[vaishnavi] Press Ctrl+C or run ./stop-server.sh to stop.\n")

    httpd = HTTPServer(("127.0.0.1", PORT), VaishnaviHandler)
    try:
        httpd.serve_forever()
    finally:
        remove_pid()
