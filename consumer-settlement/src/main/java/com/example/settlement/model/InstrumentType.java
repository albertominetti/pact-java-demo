package com.example.settlement.model;

/**
 * Instrument type: settlement only needs to distinguish instruments with a
 * maturity date (BOND) from those without one (EQUITY, ETF).
 */
public enum InstrumentType {
    BOND,
    EQUITY,
    ETF
}
