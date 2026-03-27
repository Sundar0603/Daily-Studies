#!/bin/bash
# ─────────────────────────────────────────────────────
# Daily Studies – Backend Launcher
# Starts the Spring Boot backend, opens Chrome,
# and stops everything when Chrome window is closed.
# ─────────────────────────────────────────────────────

BACKEND_DIR="$( cd "$( dirname "$0" )" && pwd )/backend"
MVN="/opt/homebrew/bin/mvn"
PID_FILE="/tmp/daily-studies-backend.pid"
URL="http://localhost:8086/"

# ── Cleanup: kill backend when this script exits ────
cleanup() {
    if [[ -f "$PID_FILE" ]]; then
        PID=$(cat "$PID_FILE")
        if kill -0 "$PID" 2>/dev/null; then
            kill "$PID"
            wait "$PID" 2>/dev/null
        fi
        rm -f "$PID_FILE"
    fi
}
trap cleanup EXIT INT TERM

# ── Check Maven ─────────────────────────────────────
if [[ ! -x "$MVN" ]]; then
    osascript -e 'display dialog "Maven not found at '"$MVN"'. Please install it." buttons {"OK"} default button "OK" with icon stop'
    exit 1
fi

# ── Start backend ───────────────────────────────────
cd "$BACKEND_DIR" || {
    osascript -e 'display dialog "Backend folder not found at '"$BACKEND_DIR"'" buttons {"OK"} default button "OK" with icon stop'
    exit 1
}

"$MVN" spring-boot:run &
echo $! > "$PID_FILE"

# ── Wait for backend to be ready ────────────────────
echo "Waiting for backend to start on port 8086..."
while ! curl -s -o /dev/null -w '' "$URL" 2>/dev/null; do
    # If backend process died, exit
    if ! kill -0 "$(cat "$PID_FILE")" 2>/dev/null; then
        echo "Backend failed to start."
        exit 1
    fi
    sleep 2
done
echo "Backend is ready!"

# ── Open Chrome in a new window ─────────────────────
osascript -e '
tell application "Google Chrome"
    activate
    set newWindow to make new window
    set URL of active tab of newWindow to "'"$URL"'"
    set windowId to id of newWindow
end tell
'

# ── Poll: wait until that Chrome window is closed ───
while true; do
    WINDOW_OPEN=$(osascript -e '
    tell application "System Events"
        if not (exists process "Google Chrome") then
            return "no"
        end if
    end tell
    tell application "Google Chrome"
        set windowCount to 0
        repeat with w in windows
            set tabURLs to URL of tabs of w
            repeat with t in tabURLs
                if t starts with "http://localhost:8086" then
                    set windowCount to windowCount + 1
                end if
            end repeat
        end repeat
        if windowCount > 0 then
            return "yes"
        else
            return "no"
        end if
    end tell
    ' 2>/dev/null)

    if [[ "$WINDOW_OPEN" != "yes" ]]; then
        echo "Chrome tab closed. Shutting down backend..."
        exit 0
    fi
    sleep 3
done
