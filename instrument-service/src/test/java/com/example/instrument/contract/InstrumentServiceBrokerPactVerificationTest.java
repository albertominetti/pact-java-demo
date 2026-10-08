package com.example.instrument.contract;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.PactBrokerAuth;
import au.com.dius.pact.provider.junitsupport.loader.VersionSelector;
import org.junit.jupiter.api.condition.EnabledIf;

/**
 * "broker" mode: verifies the contracts taken from the Pact Broker, selecting the
 * pacts published by the consumers on the "main" tag.
 *
 * Activated with:  mvn -pl instrument-service test -Dpact.broker.enabled=true
 * (or export PACT_BROKER_ENABLED=true).
 *
 * URL and credentials resolve ${...} expressions against the Spring Environment,
 * so they read (in order) system properties, the test application.yml and the
 * environment variables PACT_BROKER_BASE_URL / PACT_BROKER_USERNAME /
 * PACT_BROKER_PASSWORD.
 */
@Provider("instrument-service")
@PactBroker(
        url = "${pactbroker.url:http://localhost:9292}",
        authentication = @PactBrokerAuth(
                username = "${pactbroker.auth.username:pact}",
                password = "${pactbroker.auth.password:pact}"),
        consumerVersionSelectors = @VersionSelector(tag = "main"))
@EnabledIf("brokerModeEnabled")
class InstrumentServiceBrokerPactVerificationTest extends AbstractPactVerificationTest {

    static boolean brokerModeEnabled() {
        return PactVerificationMode.brokerEnabled();
    }
}
