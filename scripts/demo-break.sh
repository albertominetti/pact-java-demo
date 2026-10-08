#!/usr/bin/env bash
#
# DEMO-BREAK: demonstrates the value of contract testing by breaking the contract
# on the producer side and showing that the provider verification FAILS.
#
# Applied breaking change: the "price" field is renamed to "px" in the producer's
# Quote record -> the producer's JSON no longer contains the field the trading
# dashboard expects ("lastQuote.price").
#
# The file is always restored (trap), whether the verification fails or passes.
set -uo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
FILE="$ROOT_DIR/instrument-service/src/main/java/com/example/instrument/domain/Instrument.java"

if ! ls "$ROOT_DIR/pacts"/*.json >/dev/null 2>&1; then
  echo "ERROR: run 'make consumer-tests' first (the pacts in <root>/pacts are required)" >&2
  exit 1
fi

cp "$FILE" "$FILE.bak"
restore() {
  if [ -f "$FILE.bak" ]; then
    mv -f "$FILE.bak" "$FILE"
    echo "File restored: $FILE"
  fi
}
trap restore EXIT

sed -i 's/BigDecimal price,/BigDecimal px,/' "$FILE"
echo "=============================================================="
echo "Breaking change applied: lastQuote.price -> lastQuote.px"
echo "=============================================================="
echo
echo "Running the provider verification (a FAILURE is expected)..."
if mvn -q -f "$ROOT_DIR/pom.xml" -pl instrument-service test; then
  echo
  echo "ATTENTION: the verification PASSED: was the contract not broken?!"
  echo "(This example needs the generated pacts: run 'make consumer-tests' first.)"
  exit 1
else
  echo
  echo "=============================================================="
  echo "Verification FAILED as expected: the contract caught the"
  echo "breaking change BEFORE the deploy. This is the value of"
  echo "contract testing: the producer finds out immediately that"
  echo "the JSON no longer matches the consumer's expectations."
  echo "=============================================================="
fi
