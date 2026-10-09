#!/usr/bin/env bash
#
# Starts the Pact Broker + Postgres (requires Docker with the compose plugin).
set -euo pipefail

cd "$(dirname "$0")/.."

exec docker compose up -d
