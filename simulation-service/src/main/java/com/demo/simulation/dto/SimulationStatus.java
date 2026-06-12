package com.demo.simulation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulationStatus {
    private boolean active;
    private String scenario;
    private int requestsTotal;
    private int requestsCompleted;
    private int requestsSuccessful;
    private int requestsFailed;
    private double averageLatencyMs;
    private double throughputRequestsPerSec;
    private Map<String, Integer> errorCounts;
    private List<String> logs;
}
