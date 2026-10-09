package com.example.instrument.contract;

/**
 * Selector of the contract verification mode.
 *
 * - "folder" mode (DEFAULT): the pacts are read from the shared project folder
 *   (<root>/pacts) -> no broker, fully offline.
 * - "broker" mode: the pacts are read from the Pact Broker selecting the "main"
 *   tag; activated with -Dpact.broker.enabled=true or PACT_BROKER_ENABLED=true.
 *
 * The two modes are mutually exclusive: every test class enables/disables itself
 * with an @EnabledIf that calls these methods.
 */
public final class PactVerificationMode {

    private PactVerificationMode() {
    }

    public static boolean brokerEnabled() {
        String fromProperty = System.getProperty("pact.broker.enabled");
        String raw = fromProperty != null
                ? fromProperty
                : System.getenv().getOrDefault("PACT_BROKER_ENABLED", "false");
        return "true".equalsIgnoreCase(raw.trim());
    }

    public static boolean folderEnabled() {
        return !brokerEnabled();
    }
}
