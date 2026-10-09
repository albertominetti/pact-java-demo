#!/usr/bin/env bash
#
# Provider verification for instrument-service.
#
#   ./scripts/verify-provider.sh            -> folder mode (default, offline)
#   ./scripts/verify-provider.sh --broker   -> broker mode (requires the broker on
#                                              PACT_BROKER_BASE_URL, tag "main")
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

CMD=(mvn -q -pl instrument-service test)

if [ "${1:-}" = "--broker" ] || [ "${PACT_BROKER_MODE:-false}" = "true" ]; then
  PACT_BROKER_BASE_URL="${PACT_BROKER_BASE_URL:-http://localhost:9292}"
  CMD+=(-Dpact.broker.enabled=true "-Dpactbroker.url=$PACT_BROKER_BASE_URL")
  echo "BROKER mode: $PACT_BROKER_BASE_URL (selector: tag main)"
else
  echo "FOLDER mode (default): reading from <root>/pacts"
fi

exec "${CMD[@]}"
