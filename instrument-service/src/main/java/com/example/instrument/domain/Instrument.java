package com.example.instrument.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Financial instrument as exposed by the REST API.
 *
 * The produced JSON is deliberately a "superset": it contains all the fields
 * anyone might want to read. The two consumers only use a subset of it:
 *   - trading-dashboard    -> id, venue, currency, lastQuote.*
 *   - settlement-service   -> id, isin, mic, venue, instrumentType, maturityDate
 *
 * Note for the demo-break: renaming the "price" component of the Quote record
 * to "px" breaks the trading contract (the expected field "price" is not found).
 */
public record Instrument(
        String id,
        String isin,
        String name,
        String mic,
        String venue,
        InstrumentType instrumentType,
        String currency,
        LocalDate maturityDate,
        Quote lastQuote) {

    /**
     * Latest quote: null when the instrument has no live quote
     * (or no continuous market).
     */
    public record Quote(
            BigDecimal price,
            BigDecimal bid,
            BigDecimal ask,
            long volume,
            String currency,
            Instant timestamp) {
    }
}
