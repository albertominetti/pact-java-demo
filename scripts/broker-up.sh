#!/usr/bin/env bash
#
# Starts the Pact Broker + Postgres.
# It prefers 'docker compose' if the plugin is available; otherwise it uses
# equivalent 'docker run' commands (in some containers the compose plugin is missing).
set -euo pipefail

cd "$(dirname "$0")/.."

if docker compose version >/dev/null 2>&1; then
  echo "Starting with docker compose..."
  exec docker compose up -d
fi

echo "The 'docker compose' plugin is not available: starting with equivalent 'docker run' commands."

docker network create pact-demo-net >/dev/null 2>&1 || true
docker rm -f pact-demo-postgres pact-demo-broker >/dev/null 2>&1 || true

echo "- Postgres 16..."
docker run -d --name pact-demo-postgres --network pact-demo-net \
  -e POSTGRES_USER=pact \
  -e POSTGRES_PASSWORD=pact \
  -e POSTGRES_DB=pactbroker \
  -v pact-demo-pgdata:/var/lib/postgresql/data \
  postgres:16 >/dev/null

echo "  waiting for Postgres to be ready..."
for _ in $(seq 1 30); do
  if docker exec pact-demo-postgres pg_isready -U pact -d pactbroker >/dev/null 2>&1; then
    break
  fi
  sleep 1
done

echo "- Pact Broker..."
docker run -d --name pact-demo-broker --network pact-demo-net -p 9292:80 \
  -e PACT_BROKER_DATABASE_ADAPTER=postgres \
  -e PACT_BROKER_DATABASE_HOSTNAME=pact-demo-postgres \
  -e PACT_BROKER_DATABASE_NAME=pactbroker \
  -e PACT_BROKER_DATABASE_USERNAME=pact \
  -e PACT_BROKER_DATABASE_PASSWORD=pact \
  -e PACT_BROKER_BASIC_AUTH_USERNAME=pact \
  -e PACT_BROKER_BASIC_AUTH_PASSWORD=pact \
  -e PACT_BROKER_ALLOW_PUBLIC_READ=true \
  pactfoundation/pact-broker:latest >/dev/null

echo "Pact Broker: http://localhost:9292 (basic auth: pact/pact)"
