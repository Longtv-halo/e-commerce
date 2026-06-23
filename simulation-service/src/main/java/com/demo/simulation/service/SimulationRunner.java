package com.demo.simulation.service;

import com.demo.simulation.dto.SimulationRequest;
import com.demo.simulation.dto.SimulationStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimulationRunner {

    private final ObjectMapper objectMapper;
    private final MockDataGenerator dataGenerator;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(5))
            .build();

    @Value("${target.url}")
    private String targetUrl;

    // Simulation running state
    private final AtomicBoolean active = new AtomicBoolean(false);
    private String currentScenario = "NONE";
    private final AtomicInteger requestsTotal = new AtomicInteger(0);
    private final AtomicInteger requestsCompleted = new AtomicInteger(0);
    private final AtomicInteger requestsSuccessful = new AtomicInteger(0);
    private final AtomicInteger requestsFailed = new AtomicInteger(0);
    private final AtomicLong totalLatencyMs = new AtomicLong(0);
    
    private final Map<String, AtomicInteger> errorCounts = new ConcurrentHashMap<>();
    private final Queue<String> logs = new ConcurrentLinkedQueue<>();
    private long startTimeMs = 0;
    private long endTimeMs = 0;

    private ExecutorService executorService;

    public SimulationStatus getStatus() {
        int completed = requestsCompleted.get();
        int total = requestsTotal.get();
        int successful = requestsSuccessful.get();
        int failed = requestsFailed.get();
        long latency = totalLatencyMs.get();
        
        double avgLatency = completed > 0 ? (double) latency / completed : 0.0;
        
        long timeElapsedMs;
        if (active.get()) {
            timeElapsedMs = System.currentTimeMillis() - startTimeMs;
        } else {
            timeElapsedMs = endTimeMs - startTimeMs;
        }
        if (timeElapsedMs <= 0) timeElapsedMs = 1;
        double throughput = (double) completed / (timeElapsedMs / 1000.0);

        Map<String, Integer> errors = new HashMap<>();
        errorCounts.forEach((k, v) -> errors.put(k, v.get()));

        List<String> logList = new ArrayList<>(logs);
        // Reverse so newest logs are first, or keep order. We'll keep order and let frontend handle it.
        
        return SimulationStatus.builder()
                .active(active.get())
                .scenario(currentScenario)
                .requestsTotal(total)
                .requestsCompleted(completed)
                .requestsSuccessful(successful)
                .requestsFailed(failed)
                .averageLatencyMs(avgLatency)
                .throughputRequestsPerSec(throughput)
                .errorCounts(errors)
                .logs(logList)
                .build();
    }

    public synchronized boolean start(SimulationRequest request) {
        if (active.get()) {
            log.warn("Simulation is already running");
            return false;
        }

        // Clean state
        active.set(true);
        currentScenario = request.getScenario();
        requestsTotal.set(request.getRequests());
        requestsCompleted.set(0);
        requestsSuccessful.set(0);
        requestsFailed.set(0);
        totalLatencyMs.set(0);
        errorCounts.clear();
        logs.clear();
        startTimeMs = System.currentTimeMillis();
        endTimeMs = 0;

        String target = request.getTargetUrl() != null && !request.getTargetUrl().isBlank() 
                ? request.getTargetUrl() : targetUrl;

        // Build a thread pool matching the requested concurrency
        int threadCount = Math.max(1, Math.min(request.getConcurrency(), 50));
        executorService = Executors.newFixedThreadPool(threadCount);

        log.info("Starting simulation scenario: {} | Total requests: {} | Concurrency: {}", 
                currentScenario, request.getRequests(), threadCount);
        addLog("SYSTEM", "Starting scenario " + currentScenario + " against " + target + " with " + threadCount + " workers.");

        // Dispatch requests asynchronously
        int totalRequests = request.getRequests();
        for (int i = 0; i < totalRequests; i++) {
            final int index = i;
            executorService.submit(() -> {
                if (!active.get()) return;
                
                long start = System.currentTimeMillis();
                try {
                    executeScenarioStep(currentScenario, target, index);
                } catch (Exception e) {
                    requestsFailed.incrementAndGet();
                    requestsCompleted.incrementAndGet();
                    String errorKey = "Exception: " + e.getClass().getSimpleName();
                    errorCounts.computeIfAbsent(errorKey, k -> new AtomicInteger(0)).incrementAndGet();
                    addLog("ERROR", "Request failed with exception: " + e.getMessage());
                } finally {
                    long duration = System.currentTimeMillis() - start;
                    totalLatencyMs.addAndGet(duration);
                    
                    int comp = requestsCompleted.get();
                    if (comp >= totalRequests) {
                        stopSimulation();
                    }
                }
            });
        }

        return true;
    }

    public synchronized void stop() {
        if (!active.get()) return;
        addLog("SYSTEM", "Stop requested by user.");
        stopSimulation();
    }

    private void stopSimulation() {
        if (!active.get()) return;
        active.set(false);
        endTimeMs = System.currentTimeMillis();
        if (executorService != null) {
            executorService.shutdownNow();
        }
        addLog("SYSTEM", "Simulation completed/stopped. Total requests executed: " + requestsCompleted.get());
        log.info("Simulation completed. Total executed: {}", requestsCompleted.get());
    }

    private void executeScenarioStep(String scenario, String target, int stepIndex) throws Exception {
        Map<String, Object> cache = dataGenerator.getSeedCache();
        boolean hasCache = cache.containsKey("initialized") && (boolean) cache.get("initialized");

        if (!hasCache && !"AUTH_EXHAUSTION".equals(scenario)) {
            // If the seed cache hasn't been created yet, let's register users or fetch an admin token
            // dynamically to not fail immediately.
            addLog("SYSTEM", "Warning: Seed data not initialized. Running dynamic initialization...");
            dataGenerator.generateSeedData(new ArrayList<>());
            cache = dataGenerator.getSeedCache();
        }

        switch (scenario) {
            case "ABAC_VIOLATION" -> runAbacStep(target, cache, stepIndex);
            case "DB_STRESS" -> runDbStressStep(target, cache, stepIndex);
            case "LATENCY_FLOOD" -> runLatencyFloodStep(target, cache, stepIndex);
            case "AUTH_EXHAUSTION" -> runAuthExhaustionStep(target, stepIndex);
            case "MINIO_STRESS" -> runMinioStressStep(target, cache, stepIndex);
            default -> throw new IllegalArgumentException("Unknown scenario: " + scenario);
        }
    }

    private void addLog(String category, String message) {
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        String entry = String.format("[%s] [%s] %s", time, category, message);
        logs.add(entry);
        // Cap logs queue to avoid memory leak
        while (logs.size() > 100) {
            logs.poll();
        }
    }

    private void recordResponse(int statusCode, String method, String path, long latencyMs, String errorDetail) {
        requestsCompleted.incrementAndGet();
        if (statusCode >= 200 && statusCode < 300) {
            requestsSuccessful.incrementAndGet();
            addLog("SUCCESS", String.format("%s %s -> HTTP %d (%dms)", method, path, statusCode, latencyMs));
        } else {
            requestsFailed.incrementAndGet();
            String errKey = "HTTP " + statusCode + (errorDetail != null ? ": " + errorDetail : "");
            errorCounts.computeIfAbsent(errKey, k -> new AtomicInteger(0)).incrementAndGet();
            addLog("DENIED", String.format("%s %s -> HTTP %d | %s (%dms)", method, path, statusCode, errorDetail, latencyMs));
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // Scenarios Implementation
    // ────────────────────────────────────────────────────────────────────────

    private void runAbacStep(String target, Map<String, Object> cache, int stepIndex) throws Exception {
        // We alternate requests between users of different departments:
        // user_fin (always DENY)
        // user_mkt (outside hours -> default DENY)
        // user_eng (always ALLOW)
        // user_hr (always ALLOW)
        String[] users = {"user_fin", "user_mkt", "user_eng", "user_hr"};
        String username = users[stepIndex % users.length];

        @SuppressWarnings("unchecked")
        Map<String, String> userTokens = (Map<String, String>) cache.get("userTokens");
        String token = userTokens != null ? userTokens.get(username) : null;

        if (token == null) {
            // Fallback: try to login dynamically
            token = dataGenerator.login(target, username, "Password123!");
        }

        // Hit GET /api/department/1 or 2 or 3 or 4 to trigger ABAC checks
        // We randomize the department ID from 1 to 4
        long deptId = (stepIndex % 4) + 1;
        String path = "/api/department/" + deptId;

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(target + path))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        long start = System.currentTimeMillis();
        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        long latency = System.currentTimeMillis() - start;

        String errorDetail = null;
        if (res.statusCode() != 200) {
            JsonNode root = objectMapper.readTree(res.body());
            errorDetail = root.path("error").path("message").asText("Access denied");
        }

        recordResponse(res.statusCode(), "GET", path, latency, errorDetail);
    }

    private void runDbStressStep(String target, Map<String, Object> cache, int stepIndex) throws Exception {
        // Query lists with pagination to cause heavy DB operations
        String path = "/api/employee/list";
        
        // Use user_eng token or admin token
        @SuppressWarnings("unchecked")
        Map<String, String> userTokens = (Map<String, String>) cache.get("userTokens");
        String token = userTokens != null ? userTokens.get("user_eng") : null;
        if (token == null) token = dataGenerator.login(target, "admin", "admin123");

        // Request a large page size to stress PostgreSQL
        String body = "{\"page\": 0, \"size\": 80, \"sortBy\": \"id\", \"sortDir\": \"desc\"}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(target + path))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        long start = System.currentTimeMillis();
        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        long latency = System.currentTimeMillis() - start;

        recordResponse(res.statusCode(), "POST", path, latency, res.statusCode() != 200 ? "DB Load Error" : null);
    }

    private void runLatencyFloodStep(String target, Map<String, Object> cache, int stepIndex) throws Exception {
        // Heavy unindexed name searches on employee list
        String path = "/api/employee/search";

        @SuppressWarnings("unchecked")
        Map<String, String> userTokens = (Map<String, String>) cache.get("userTokens");
        String token = userTokens != null ? userTokens.get("user_eng") : null;
        if (token == null) token = dataGenerator.login(target, "admin", "admin123");

        // Random search terms to avoid database caching (if any)
        String[] terms = {"Staff", "Leader", "Nguyen", "Tran", "Le", "Pham", "Vo", "An", "Bich", "Nam"};
        String term = terms[stepIndex % terms.length];
        String body = String.format("{\"empName\": \"%s\", \"page\": 0, \"size\": 50, \"sortBy\": \"empName\", \"sortDir\": \"asc\"}", term);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(target + path))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        long start = System.currentTimeMillis();
        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        long latency = System.currentTimeMillis() - start;

        recordResponse(res.statusCode(), "POST", path, latency, res.statusCode() != 200 ? "Search Failure" : null);
    }

    private void runAuthExhaustionStep(String target, int stepIndex) throws Exception {
        // High volume CPU login requests
        String path = "/api/auth/login";
        
        // 50% successful logins, 50% failed logins
        boolean successCase = stepIndex % 2 == 0;
        String username = successCase ? "user_eng" : "fake_user_" + stepIndex;
        String password = successCase ? "Password123!" : "WrongPassword123!";

        String body = String.format("{\"username\": \"%s\", \"password\": \"%s\"}", username, password);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(target + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        long start = System.currentTimeMillis();
        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        long latency = System.currentTimeMillis() - start;

        String err = null;
        if (res.statusCode() != 200) {
            err = res.statusCode() == 401 ? "Unauthorized (Expected)" : "Auth Error";
        }
        recordResponse(res.statusCode(), "POST", path, latency, err);
    }

    private void runMinioStressStep(String target, Map<String, Object> cache, int stepIndex) throws Exception {
        // File uploading and downloading
        @SuppressWarnings("unchecked")
        Map<String, String> userTokens = (Map<String, String>) cache.get("userTokens");
        String token = userTokens != null ? userTokens.get("user_eng") : null;
        if (token == null) token = dataGenerator.login(target, "admin", "admin123");

        // Alternating upload and download
        boolean isUpload = stepIndex % 2 == 0;
        if (isUpload) {
            String path = "/api/file/upload";
            String boundary = "---Boundary-" + System.currentTimeMillis();
            
            // Build multipart/form-data
            String fileContent = "This is a load test file upload payload. Step index: " + stepIndex;
            StringBuilder builder = new StringBuilder();
            builder.append("--").append(boundary).append("\r\n");
            builder.append("Content-Disposition: form-data; name=\"file\"; filename=\"stress_test_" + stepIndex + ".txt\"\r\n");
            builder.append("Content-Type: text/plain\r\n\r\n");
            builder.append(fileContent).append("\r\n");
            builder.append("--").append(boundary).append("--\r\n");

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(target + path))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofString(builder.toString()))
                    .build();

            long start = System.currentTimeMillis();
            HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            long latency = System.currentTimeMillis() - start;

            recordResponse(res.statusCode(), "POST", path, latency, res.statusCode() != 200 ? "Upload Failed" : null);
        } else {
            // For downloading, we need a filename.
            // Since we upload "stress_test_{stepIndex-1}.txt", let's download that file.
            String filename = "stress_test_" + (stepIndex - 1) + ".txt";
            String path = "/api/file/files/" + filename;

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(target + path))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            long start = System.currentTimeMillis();
            HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            long latency = System.currentTimeMillis() - start;

            // Downloading files returns raw content, which is successful if 200
            recordResponse(res.statusCode(), "GET", path, latency, res.statusCode() != 200 ? "Download Failed" : null);
        }
    }
}
