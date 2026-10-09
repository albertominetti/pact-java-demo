#!/usr/bin/env bash
#
# Stops and removes the Pact Broker + Postgres (equivalent of 'docker compose down').
set -euo pipefail

cd "$(dirname "$0")/.."

exec docker compose down
