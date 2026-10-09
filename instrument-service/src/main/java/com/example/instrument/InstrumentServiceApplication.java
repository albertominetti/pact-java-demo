package com.example.instrument;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * PRODUCER of the demo: REST service for financial instruments.
 * It exposes a "superset" JSON: all the fields of the model, while each
 * consumer (trading, settlement) only consumes a subset of them.
 */
@SpringBootApplication
public class InstrumentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InstrumentServiceApplication.class, args);
    }
}
