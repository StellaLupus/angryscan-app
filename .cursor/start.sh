#!/usr/bin/env bash
# Per-boot service startup for the Angry Data Scanner environment.
# Provides a persistent virtual X display so the Compose Desktop GUI can be launched
# headlessly (e.g. `DISPLAY=:99 "desktop/build/compose/binaries/main/app/Angry Data Scanner/bin/Angry Data Scanner"`).
# Automated UI tests run under their own display via `xvfb-run -a ./gradlew allTests`.
set -euo pipefail

DISPLAY_NUM=":99"

# Start Xvfb only if it is not already serving this display (idempotent across restarts).
if ! xdpyinfo -display "${DISPLAY_NUM}" >/dev/null 2>&1; then
  echo "Starting Xvfb on ${DISPLAY_NUM}..."
  Xvfb "${DISPLAY_NUM}" -screen 0 1400x900x24 >/tmp/xvfb.log 2>&1 &
  # Wait until the display is ready.
  for _ in $(seq 1 30); do
    if xdpyinfo -display "${DISPLAY_NUM}" >/dev/null 2>&1; then
      break
    fi
    sleep 0.5
  done
fi

echo "Virtual display ${DISPLAY_NUM} is ready."
