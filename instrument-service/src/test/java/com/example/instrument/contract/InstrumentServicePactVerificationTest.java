package com.example.instrument.contract;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import org.junit.jupiter.api.condition.EnabledIf;

/**
 * DEFAULT mode (offline, no broker): verifies the contracts by reading the pacts
 * from the shared project folder.
 *
 * The two consumers write their pacts into target/pacts and the build copies
 * them into <root>/pacts: here ONE single @PactFolder is enough (not repeatable
 * in pact-jvm 4.6.x).
 *
 * The path comes from the "pact.folder" system property set by the surefire
 * plugin (an absolute path); the "../pacts" default covers IDE usage.
 */
@Provider("instrument-service")
@PactFolder("${pact.folder:../pacts}")
@EnabledIf("folderModeEnabled")
class InstrumentServicePactVerificationTest extends AbstractPactVerificationTest {

    static boolean folderModeEnabled() {
        return PactVerificationMode.folderEnabled();
    }
}
