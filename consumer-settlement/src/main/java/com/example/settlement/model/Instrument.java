package com.example.settlement.model;

import java.time.LocalDate;

/**
 * The settlement/back-office view: only the master-data and maturity fields this
 * consumer reads (no price data: the back-office does not need to know what the
 * instrument is worth, only where and when it expires).
 * A deliberate subset of the contract: the producer also returns
 * name, currency and lastQuote, which are ignored here.
 */
public record Instrument(String id, String isin, String mic, String venue,
                         InstrumentType instrumentType, LocalDate maturityDate) {
}
