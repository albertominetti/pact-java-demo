# Demo Pact (contract testing) - convenient targets around the Maven commands.
#
# Typical sequence:
#   make build            -> mvn clean install -DskipTests
#   make consumer-tests   -> generates the two consumer pacts (target/pacts + <root>/pacts)
#   make publish          -> (optional, requires the broker) publishes the pacts
#   make verify           -> provider verification in folder mode (default)
#   make verify-broker    -> provider verification in broker mode
#   make demo-break       -> breaks a field and shows the verification failing

MVN ?= mvn

.PHONY: build consumer-tests publish verify verify-broker broker-up broker-down demo-break clean help

help:
	@echo "Available targets:"
	@echo "  build           full build (tests skipped)"
	@echo "  consumer-tests  generates the pacts of the two consumers"
	@echo "  publish         publishes the pacts to the Pact Broker"
	@echo "  verify          verifies the contracts (folder mode, default)"
	@echo "  verify-broker   verifies the contracts (broker mode, tag main)"
	@echo "  broker-up       starts Pact Broker + Postgres (docker)"
	@echo "  broker-down     stops Pact Broker + Postgres"
	@echo "  demo-break      demonstrative breaking change (the verification fails)"
	@echo "  clean           full cleanup (including the copies in pacts/)"

build:
	$(MVN) -q clean install -DskipTests

consumer-tests:
	$(MVN) -q -pl consumer-trading,consumer-settlement test

publish:
	./scripts/publish-pacts.sh

verify:
	$(MVN) -q -pl instrument-service test

verify-broker:
	./scripts/verify-provider.sh --broker

broker-up:
	./scripts/broker-up.sh

broker-down:
	./scripts/broker-down.sh

demo-break:
	./scripts/demo-break.sh

clean:
	$(MVN) -q clean
	rm -rf pacts
