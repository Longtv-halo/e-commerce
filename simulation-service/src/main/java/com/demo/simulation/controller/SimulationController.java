package com.demo.simulation.controller;

import com.demo.simulation.dto.SimulationRequest;
import com.demo.simulation.dto.SimulationStatus;
import com.demo.simulation.service.MockDataGenerator;
import com.demo.simulation.service.SimulationRunner;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/simulation")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SimulationController {

    private final SimulationRunner simulationRunner;
    private final MockDataGenerator mockDataGenerator;

    @PostMapping("/start")
    public ResponseEntity<Map<String, String>> start(@RequestBody SimulationRequest request) {
        boolean started = simulationRunner.start(request);
        if (started) {
            return ResponseEntity.ok(Map.of("message", "Simulation started successfully"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "Simulation is already running"));
        }
    }

    @PostMapping("/stop")
    public ResponseEntity<Map<String, String>> stop() {
        simulationRunner.stop();
        return ResponseEntity.ok(Map.of("message", "Simulation stopped successfully"));
    }

    @GetMapping("/status")
    public ResponseEntity<SimulationStatus> getStatus() {
        return ResponseEntity.ok(simulationRunner.getStatus());
    }

    @PostMapping("/generate-data")
    public ResponseEntity<Map<String, String>> generateData() {
        List<String> logAccumulator = Collections.synchronizedList(new ArrayList<>());
        // Run data generation synchronously or in a separate thread depending on complexity.
        // Since it only takes 1-2 seconds, running synchronously is fine and yields simple responses.
        String status = mockDataGenerator.generateSeedData(logAccumulator);
        if ("SUCCESS".equals(status)) {
            return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Seed data created successfully",
                "logs", String.join("\n", logAccumulator)
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "FAILED",
                "message", "Seed data creation failed: " + status,
                "logs", String.join("\n", logAccumulator)
            ));
        }
    }
}
