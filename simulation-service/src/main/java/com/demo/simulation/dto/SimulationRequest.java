package com.demo.simulation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulationRequest {
    private String scenario; // ABAC_VIOLATION, DB_STRESS, LATENCY_FLOOD, AUTH_EXHAUSTION, MINIO_STRESS
    private int requests;
    private int concurrency;
    private String targetUrl;
}
