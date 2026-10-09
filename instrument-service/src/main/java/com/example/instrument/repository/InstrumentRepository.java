package com.example.instrument.repository;

import com.example.instrument.domain.Instrument;
import com.example.instrument.domain.InstrumentType;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory repository seeded with ~5 realistic instruments (BTP, Bund, a stock,
 * an ETF and another European stock).
 *
 * The data is DETERMINISTIC: the provider states of the verification tests call
 * {@link #reset()} to bring the repository back to the known state expected by
 * the contracts.
 */
@Repository
public class InstrumentRepository {

    /** Fixed timestamp of the quotes: keeps the data reproducible in the tests. */
    private static final Instant QUOTE_TIME = Instant.parse("2026-10-08T09:30:00Z");

    private final Map<String, Instrument> instruments = new LinkedHashMap<>();

    public InstrumentRepository() {
        reset();
    }

    /** Resets the repository to its initial state (also used by the @State methods). */
    public void reset() {
        instruments.clear();
        for (Instrument instrument : seed()) {
            instruments.put(instrument.id(), instrument);
        }
    }

    public Optional<Instrument> findById(String id) {
        return Optional.ofNullable(instruments.get(id));
    }

    /** List of instruments traded on a given MIC; empty if none. */
    public List<Instrument> findByMic(String mic) {
        return instruments.values().stream()
                .filter(instrument -> instrument.mic().equals(mic))
                .toList();
    }

    public List<Instrument> findAll() {
        return List.copyOf(instruments.values());
    }

    private static List<Instrument> seed() {
        return List.of(
                // BTP: has a live quote, this is the case used by the trading dashboard
                new Instrument(
                        "IT0005443456", "IT0005443456", "BTP 4.40% 2033", "XMIL", "Borsa Italiana",
                        InstrumentType.BOND, "EUR", LocalDate.of(2033, 9, 15),
                        new Instrument.Quote(new BigDecimal("92.35"), new BigDecimal("92.30"),
                                new BigDecimal("92.40"), 1_250_000L, "EUR", QUOTE_TIME)),
                // German government bond
                new Instrument(
                        "DE0001102516", "DE0001102516", "Bund 2.60% 2033", "XETR", "Deutsche Boerse",
                        InstrumentType.BOND, "EUR", LocalDate.of(2033, 2, 15),
                        new Instrument.Quote(new BigDecimal("99.12"), new BigDecimal("99.08"),
                                new BigDecimal("99.16"), 890_000L, "EUR", QUOTE_TIME)),
                // Italian stock: on XMIL, but without a maturityDate
                new Instrument(
                        "IT0003128867", "IT0003128867", "Enel", "XMIL", "Borsa Italiana",
                        InstrumentType.EQUITY, "EUR", null,
                        new Instrument.Quote(new BigDecimal("7.42"), new BigDecimal("7.41"),
                                new BigDecimal("7.43"), 4_500_000L, "EUR", QUOTE_TIME)),
                // Global equity ETF
                new Instrument(
                        "IE00B4L5Y983", "IE00B4L5Y983", "iShares Core MSCI World", "XETR", "Deutsche Boerse",
                        InstrumentType.ETF, "EUR", null,
                        new Instrument.Quote(new BigDecimal("98.75"), new BigDecimal("98.70"),
                                new BigDecimal("98.80"), 320_000L, "EUR", QUOTE_TIME)),
                // Dutch stock on Euronext Amsterdam
                new Instrument(
                        "NL0011794037", "NL0011794037", "ASML", "AEX", "Euronext Amsterdam",
                        InstrumentType.EQUITY, "EUR", null,
                        new Instrument.Quote(new BigDecimal("680.20"), new BigDecimal("679.90"),
                                new BigDecimal("680.50"), 150_000L, "EUR", QUOTE_TIME)));
    }
}
