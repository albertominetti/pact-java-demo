package com.example.trading.client;

import com.example.trading.model.Instrument;
import org.springframework.web.client.RestClient;

/**
 * Minimal REST client used by the trading dashboard to call the
 * producer (instrument-service).
 */
public class InstrumentClient {

    private final RestClient restClient;

    /** @param baseUrl base URL of the producer, e.g. "http://localhost:8080" or the Pact mock server URL */
    public InstrumentClient(String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    /** GET /api/instruments/{id} - returns the instrument with its latest quote. */
    public Instrument getInstrument(String id) {
        return restClient.get()
                .uri("/api/instruments/{id}", id)
                .retrieve()
                .body(Instrument.class);
    }
}
