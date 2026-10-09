package com.example.trading.contract;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTest;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactDirectory;
import com.example.trading.client.InstrumentClient;
import com.example.trading.model.Instrument;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Contract test of the TRADING DASHBOARD (consumer 1).
 *
 * What this consumer wants: read one instrument and see its live quote.
 * It therefore declares ONLY these fields in the pact:
 *   id, venue, currency, lastQuote.{price, bid, ask, currency, timestamp}
 * All the other fields the producer may return (isin, name, mic,
 * instrumentType, maturityDate, lastQuote.volume, ...) do not appear here:
 * this is the "subset" that makes the contract consumer-driven.
 *
 * The pact is written to target/pacts (then copied into <root>/pacts by the build).
 */
@PactConsumerTest
@PactTestFor(providerName = "instrument-service", pactVersion = PactSpecVersion.V3)
@PactDirectory("target/pacts")
class InstrumentQuotePactTest {

    private static final String INSTRUMENT_ID = "IT0005443456";

    @Pact(provider = "instrument-service", consumer = "trading-dashboard")
    RequestResponsePact createPact(PactDslWithProvider builder) {

        // Subset of lastQuote interesting for trading: the prices and when they
        // were updated. volume is not needed (we will ask for it when it is).
        PactDslJsonBody lastQuote = new PactDslJsonBody()
                .decimalType("price")
                .decimalType("bid")
                .decimalType("ask")
                .stringType("currency", "EUR")
                .datetime("timestamp", "yyyy-MM-dd'T'HH:mm:ss'Z'");

        return builder
                .given("instrument " + INSTRUMENT_ID + " exists with a live quote")
                .uponReceiving("a request for the live quote of instrument " + INSTRUMENT_ID)
                .path("/api/instruments/" + INSTRUMENT_ID)
                .method("GET")
                .willRespondWith()
                .status(200)
                .headers(Map.of("Content-Type", "application/json"))
                .body(new PactDslJsonBody()
                        .stringType("id", INSTRUMENT_ID)
                        .stringType("venue", "Borsa Italiana")
                        .stringType("currency", "EUR")
                        .object("lastQuote", lastQuote))
                .toPact();
    }

    /**
     * A "real" test: it runs the REST client against the Pact mock server, which
     * responds exactly according to the contract. If the client cannot interpret
     * the response, this test fails HERE, before ever touching the producer.
     */
    @Test
    void clientShouldRetrieveTheLiveQuote(MockServer mockServer) {
        InstrumentClient client = new InstrumentClient(mockServer.getUrl());

        Instrument instrument = client.getInstrument(INSTRUMENT_ID);

        assertThat(instrument.id()).isEqualTo(INSTRUMENT_ID);
        assertThat(instrument.venue()).isNotBlank();
        assertThat(instrument.currency()).isEqualTo("EUR");
        assertThat(instrument.lastQuote()).isNotNull();
        assertThat(instrument.lastQuote().price()).isNotNull();
        assertThat(instrument.lastQuote().bid()).isNotNull();
        assertThat(instrument.lastQuote().ask()).isNotNull();
        assertThat(instrument.lastQuote().timestamp()).isNotNull();
    }
}
