#!/bin/bash
# stop-server.sh — Stop the Vaishnavi dev backend server
#
# Usage:
#   ./stop-server.sh
#
# Strategy (in order):
#   1. Read PID from .vaishnavi.pid and kill that process.
#   2. Fall back to finding the process by port 9000 (lsof or ss).
#   3. Report if nothing was found.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PID_FILE="$SCRIPT_DIR/.vaishnavi.pid"
PORT=9000

# ── Helper: kill a PID and confirm ─────────────────────────────────────────
kill_pid() {
    local pid="$1"
    if kill -0 "$pid" 2>/dev/null; then
        kill "$pid"
        echo "✅ Vaishnavi server (PID $pid) stopped."
        return 0
    else
        return 1
    fi
}

# ── Strategy 1: PID file ────────────────────────────────────────────────────
if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if kill_pid "$PID"; then
        rm -f "$PID_FILE"
        exit 0
    else
        echo "⚠️  Stale PID file found (PID $PID no longer running). Removing it."
        rm -f "$PID_FILE"
    fi
fi

# ── Strategy 2: find by port (lsof preferred, ss as fallback) ───────────────
PID=""

if command -v lsof &>/dev/null; then
    PID=$(lsof -ti "tcp:$PORT" 2>/dev/null | head -1 || true)
elif command -v ss &>/dev/null; then
    PID=$(ss -tlnp "sport = :$PORT" 2>/dev/null \
        | awk '/LISTEN/{match($0,/pid=([0-9]+)/,a); if(a[1]) print a[1]}' \
        | head -1 || true)
fi

if [ -n "$PID" ]; then
    kill_pid "$PID"
    exit 0
fi

echo "ℹ️  No Vaishnavi server found on port $PORT. Nothing to stop."
