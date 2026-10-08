package com.example.settlement.contract;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTest;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactDirectory;
import com.example.settlement.client.InstrumentClient;
import com.example.settlement.model.Instrument;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Contract test of the SETTLEMENT / BACK-OFFICE (consumer 2).
 *
 * What this consumer wants: the list of instruments traded on a MIC, with
 * master data and maturity. It declares ONLY these fields:
 *   id, isin, mic, venue, instrumentType, maturityDate
 * No prices at all: it is a DIFFERENT subset from the one used by trading,
 * even though it starts from the same endpoint/JSON of the producer.
 *
 * The body is an "exact" JSON array (the seed data is deterministic):
 * this shows the other contract style, besides the type matchers used by trading.
 */
@PactConsumerTest
@PactTestFor(providerName = "instrument-service", pactVersion = PactSpecVersion.V3)
@PactDirectory("target/pacts")
class InstrumentsByMicPactTest {

    private static final String XMIL_BODY = """
            [
              {
                "id": "IT0005443456",
                "isin": "IT0005443456",
                "mic": "XMIL",
                "venue": "Borsa Italiana",
                "instrumentType": "BOND",
                "maturityDate": "2033-09-15"
              },
              {
                "id": "IT0003128867",
                "isin": "IT0003128867",
                "mic": "XMIL",
                "venue": "Borsa Italiana",
                "instrumentType": "EQUITY",
                "maturityDate": null
              }
            ]""";

    @Pact(provider = "instrument-service", consumer = "settlement-service")
    RequestResponsePact createPact(PactDslWithProvider builder) {
        return builder
                .given("instruments exist for MIC XMIL")
                .uponReceiving("a request for the instruments traded on MIC XMIL")
                .path("/api/instruments")
                .query("mic=XMIL")
                .method("GET")
                .willRespondWith()
                .status(200)
                .headers(Map.of("Content-Type", "application/json"))
                .body(XMIL_BODY)
                .toPact();
    }

    /**
     * A "real" test: the back-office client queried against the Pact mock server.
     * It verifies that the list can be deserialized and contains the master data.
     */
    @Test
    void clientShouldRetrieveInstrumentsForXmil(MockServer mockServer) {
        InstrumentClient client = new InstrumentClient(mockServer.getUrl());

        List<Instrument> instruments = client.findByMic("XMIL");

        assertThat(instruments).hasSize(2);
        Instrument bond = instruments.get(0);
        assertThat(bond.id()).isEqualTo("IT0005443456");
        assertThat(bond.isin()).isEqualTo("IT0005443456");
        assertThat(bond.mic()).isEqualTo("XMIL");
        assertThat(bond.instrumentType()).isEqualTo(com.example.settlement.model.InstrumentType.BOND);
        assertThat(bond.maturityDate()).isNotNull();

        Instrument equity = instruments.get(1);
        assertThat(equity.instrumentType()).isEqualTo(com.example.settlement.model.InstrumentType.EQUITY);
        assertThat(equity.maturityDate()).isNull();
    }
}
