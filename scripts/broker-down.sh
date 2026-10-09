#!/usr/bin/env bash
#
# Stops and removes the Pact Broker + Postgres (equivalent of 'docker compose down').
set -euo pipefail

cd "$(dirname "$0")/.."

if docker compose version >/dev/null 2>&1; then
  exec docker compose down
fi

echo "The 'docker compose' plugin is not available: stopping the containers created with docker run."
docker rm -f pact-demo-broker pact-demo-postgres >/dev/null 2>&1 || true
echo "Done (the pact-demo-pgdata volume is kept; remove it with"
echo " docker volume rm pact-demo-pgdata if you want to reset the data)."
