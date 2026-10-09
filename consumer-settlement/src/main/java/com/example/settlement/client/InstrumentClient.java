package com.example.settlement.client;

import com.example.settlement.model.Instrument;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Objects;

/**
 * Minimal REST client used by the settlement/back-office to call the
 * producer (instrument-service).
 */
public class InstrumentClient {

    private static final ParameterizedTypeReference<List<Instrument>> INSTRUMENT_LIST =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;

    /** @param baseUrl base URL of the producer, e.g. "http://localhost:8080" or the Pact mock server URL */
    public InstrumentClient(String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    /** GET /api/instruments?mic={mic} - list of instruments traded on a given MIC. */
    public List<Instrument> findByMic(String mic) {
        List<Instrument> instruments = restClient.get()
                .uri("/api/instruments?mic={mic}", mic)
                .retrieve()
                .body(INSTRUMENT_LIST);
        return Objects.requireNonNull(instruments, "empty response for mic=" + mic);
    }
}
