package com.example.instrument.contract;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.spring.junit5.PactVerificationSpringProvider;
import com.example.instrument.InstrumentServiceApplication;
import com.example.instrument.repository.InstrumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Base class shared by the contract verification (provider verification).
 *
 * - @SpringBootTest with WebEnvironment.RANDOM_PORT: starts the real application
 *   on a free port; the Pact verifier talks to it over real HTTP, exercising the
 *   whole stack (controller, JSON serialization, ...).
 * - PactVerificationSpringProvider: the pact-jvm extension that hooks into the
 *   Spring context and generates one test per interaction of the pacts found.
 * - @State: the provider states declared by the consumers act as seeds: they
 *   bring the in-memory repository back to the state the contract expects.
 *
 * The concrete subclasses add the pact source (a single @PactFolder per class:
 * in pact-jvm 4.7.x the annotation is not repeatable) and the folder/broker mode
 * gate.
 */
@SpringBootTest(classes = InstrumentServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
abstract class AbstractPactVerificationTest {

    @Autowired
    private InstrumentRepository repository;

    @LocalServerPort
    private int port;

    /** Points the verifier's HTTP target at the port Spring Boot started the app on. */
    @BeforeEach
    void configureTestTarget(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", port));
    }

    @TestTemplate
    @ExtendWith(PactVerificationSpringProvider.class)
    void verifyPactInteraction(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @State("instrument IT0005443456 exists with a live quote")
    public void seedInstrumentWithLiveQuote() {
        repository.reset();
    }

    @State("instruments exist for MIC XMIL")
    public void seedInstrumentsForMicXmil() {
        repository.reset();
    }
}
