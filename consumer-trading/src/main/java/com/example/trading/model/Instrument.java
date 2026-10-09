package com.example.trading.model;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * The trading dashboard's view of an instrument: it contains ONLY the fields this
 * consumer actually reads. The producer returns many other fields (isin, name,
 * mic, instrumentType, maturityDate, volume, ...): they are not needed here and are
 * ignored thanks to FAIL_ON_UNKNOWN_PROPERTIES being disabled (Spring's default).
 * A didactic example of a contract "subset".
 */
public record Instrument(String id, String venue, String currency, LastQuote lastQuote) {

    /** Live quote: the data that the trading dashboard displays. */
    public record LastQuote(BigDecimal price, BigDecimal bid, BigDecimal ask, String currency, Instant timestamp) {
    }
}
