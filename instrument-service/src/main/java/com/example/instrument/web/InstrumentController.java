package com.example.instrument.web;

import com.example.instrument.domain.Instrument;
import com.example.instrument.repository.InstrumentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST API of the producer:
 *
 *   GET /api/instruments/{id}       -> full instrument (404 if it does not exist)
 *   GET /api/instruments?mic={mic}  -> list filtered by MIC (empty list if none)
 */
@RestController
@RequestMapping("/api/instruments")
public class InstrumentController {

    private final InstrumentRepository repository;

    public InstrumentController(InstrumentRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Instrument> byId(@PathVariable String id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<Instrument> byMic(@RequestParam(name = "mic", required = false) String mic) {
        if (mic == null || mic.isBlank()) {
            return repository.findAll();
        }
        return repository.findByMic(mic);
    }
}
