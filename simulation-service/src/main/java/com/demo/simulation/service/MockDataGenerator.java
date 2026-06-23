package com.demo.simulation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MockDataGenerator {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder().build();

    @Value("${target.url}")
    private String targetUrl;

    @Value("${target.admin.username}")
    private String adminUsername;

    @Value("${target.admin.password}")
    private String adminPassword;

    // Helper map to cache generated info for simulation runs
    private final Map<String, Object> seedCache = new HashMap<>();

    public Map<String, Object> getSeedCache() {
        return seedCache;
    }

    public String login(String url, String username, String password) throws Exception {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("username", username);
        body.put("password", password);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Login failed with status " + response.statusCode() + ": " + response.body());
        }

        JsonNode node = objectMapper.readTree(response.body());
        if (!node.path("success").asBoolean()) {
            throw new RuntimeException("Login failed: " + node.path("error").path("message").asText());
        }

        return node.path("results").path("token").asText();
    }

    public synchronized String generateSeedData(List<String> logAccumulator) {
        try {
            logAccumulator.add("Starting data generation...");
            log.info("Starting mock data generation targeting {}", targetUrl);

            // 1. Login as admin
            String token;
            try {
                logAccumulator.add("Attempting admin login as admin_new/demo123...");
                token = login(targetUrl, "admin_new", "demo123");
            } catch (Exception e) {
                logAccumulator.add("admin_new login failed, falling back to admin/admin123...");
                token = login(targetUrl, "admin", "admin123");
            }
            logAccumulator.add("Admin login successful. Token acquired.");

            // 2. Ensure POLICY_MANAGE permission exists and is assigned to ROLE_ADMIN
            ensurePolicyManagePermission(token, logAccumulator);

            // 3. Create Departments and get their IDs
            Map<String, Long> departments = createDepartments(token, logAccumulator);

            // 4. Create Employees under these departments
            Map<String, List<Long>> employees = createEmployees(token, departments, logAccumulator);

            // 5. Register and link users
            Map<String, String> userTokens = registerAndLinkUsers(token, departments, employees, logAccumulator);

            // 6. Set up ABAC policies to support different scenarios
            setupAbacPolicies(token, departments, logAccumulator);

            seedCache.put("departments", departments);
            seedCache.put("employees", employees);
            seedCache.put("userTokens", userTokens);
            seedCache.put("initialized", true);

            logAccumulator.add("Seeding completed successfully!");
            log.info("Mock data generation completed.");
            return "SUCCESS";
        } catch (Exception e) {
            String errMsg = "Seeding failed: " + e.getMessage();
            log.error(errMsg, e);
            logAccumulator.add(errMsg);
            return "FAILED: " + e.getMessage();
        }
    }

    private void ensurePolicyManagePermission(String adminToken, List<String> logAccumulator) throws Exception {
        logAccumulator.add("Checking for POLICY_MANAGE permission...");
        
        // Get all permissions
        HttpRequest getPermsReq = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl + "/api/permission"))
                .header("Authorization", "Bearer " + adminToken)
                .GET()
                .build();
        
        HttpResponse<String> permsRes = httpClient.send(getPermsReq, HttpResponse.BodyHandlers.ofString());
        boolean permissionExists = false;
        Long policyManageId = null;

        if (permsRes.statusCode() == 200) {
            JsonNode root = objectMapper.readTree(permsRes.body());
            JsonNode results = root.path("results");
            if (results.isArray()) {
                for (JsonNode perm : results) {
                    if ("POLICY_MANAGE".equals(perm.path("name").asText())) {
                        permissionExists = true;
                        policyManageId = perm.path("id").asLong();
                        break;
                    }
                }
            }
        }

        if (!permissionExists) {
            logAccumulator.add("POLICY_MANAGE permission not found. Creating it...");
            ObjectNode body = objectMapper.createObjectNode();
            body.put("name", "POLICY_MANAGE");
            body.put("description", "Manage ABAC access policies");

            HttpRequest createPermReq = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl + "/api/permission/create"))
                    .header("Authorization", "Bearer " + adminToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> createRes = httpClient.send(createPermReq, HttpResponse.BodyHandlers.ofString());
            if (createRes.statusCode() == 201 || createRes.statusCode() == 200) {
                JsonNode resNode = objectMapper.readTree(createRes.body());
                policyManageId = resNode.path("results").path("id").asLong();
                logAccumulator.add("Created POLICY_MANAGE permission successfully. ID: " + policyManageId);
            } else {
                logAccumulator.add("Warning: Could not create POLICY_MANAGE: " + createRes.body());
            }
        } else {
            logAccumulator.add("POLICY_MANAGE permission already exists (ID: " + policyManageId + ").");
        }

        // Get ROLE_ADMIN to check if it has the permission
        HttpRequest getRolesReq = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl + "/api/role"))
                .header("Authorization", "Bearer " + adminToken)
                .GET()
                .build();

        HttpResponse<String> rolesRes = httpClient.send(getRolesReq, HttpResponse.BodyHandlers.ofString());
        if (rolesRes.statusCode() == 200) {
            JsonNode root = objectMapper.readTree(rolesRes.body());
            JsonNode roles = root.path("results");
            Long adminRoleId = null;
            List<Long> currentPermissionIds = new ArrayList<>();
            boolean adminHasPolicyManage = false;

            if (roles.isArray()) {
                for (JsonNode role : roles) {
                    if ("ROLE_ADMIN".equals(role.path("name").asText())) {
                        adminRoleId = role.path("id").asLong();
                        // Get all permissions of ROLE_ADMIN
                        // Note: The response contains names of permissions, not necessarily IDs.
                        // We will look up all permission IDs from /api/permission and add them.
                        adminHasPolicyManage = role.path("permissions").toString().contains("POLICY_MANAGE");
                        break;
                    }
                }
            }

            if (adminRoleId != null && !adminHasPolicyManage && policyManageId != null) {
                logAccumulator.add("Assigning POLICY_MANAGE to ROLE_ADMIN...");
                
                // Fetch all permission IDs to assign to ROLE_ADMIN
                List<Long> allPermIds = new ArrayList<>();
                HttpResponse<String> allPermsRes = httpClient.send(getPermsReq, HttpResponse.BodyHandlers.ofString());
                if (allPermsRes.statusCode() == 200) {
                    JsonNode allPermsRoot = objectMapper.readTree(allPermsRes.body());
                    for (JsonNode p : allPermsRoot.path("results")) {
                        allPermIds.add(p.path("id").asLong());
                    }
                }
                if (!allPermIds.contains(policyManageId)) {
                    allPermIds.add(policyManageId);
                }

                ArrayNode permIdsArray = objectMapper.valueToTree(allPermIds);
                HttpRequest assignReq = HttpRequest.newBuilder()
                        .uri(URI.create(targetUrl + "/api/role/" + adminRoleId + "/permissions"))
                        .header("Authorization", "Bearer " + adminToken)
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(permIdsArray.toString()))
                        .build();

                HttpResponse<String> assignRes = httpClient.send(assignReq, HttpResponse.BodyHandlers.ofString());
                if (assignRes.statusCode() == 200) {
                    logAccumulator.add("Successfully assigned POLICY_MANAGE to ROLE_ADMIN.");
                } else {
                    logAccumulator.add("Warning: Could not assign permission to admin role: " + assignRes.body());
                }
            }
        }
    }

    private Map<String, Long> createDepartments(String adminToken, List<String> logAccumulator) throws Exception {
        String[] depts = {"Engineering", "Marketing", "HR", "Finance"};
        Map<String, Long> created = new HashMap<>();

        for (String name : depts) {
            logAccumulator.add("Creating department '" + name + "'...");
            ObjectNode body = objectMapper.createObjectNode();
            body.put("name", name);
            body.put("empEmail", name.toLowerCase() + ".leader@company.vn");
            body.put("empName", name + " Leader");
            body.put("isOwner", true);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl + "/api/department/create"))
                    .header("Authorization", "Bearer " + adminToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() == 201 || res.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(res.body());
                long id = root.path("results").path("id").asLong();
                created.put(name, id);
                logAccumulator.add("Department '" + name + "' created with ID: " + id);
            } else {
                // Check if department already exists (retrieve ID via search/list)
                logAccumulator.add("Department '" + name + "' create returned " + res.statusCode() + ". Fetching existing ID...");
                Long existingId = findDepartmentIdByName(adminToken, name);
                if (existingId != null) {
                    created.put(name, existingId);
                    logAccumulator.add("Found existing department '" + name + "' with ID: " + existingId);
                } else {
                    throw new RuntimeException("Could not create department " + name + ": " + res.body());
                }
            }
        }
        return created;
    }

    private Long findDepartmentIdByName(String adminToken, String name) throws Exception {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("departmentName", name);
        body.put("page", 0);
        body.put("size", 10);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl + "/api/department/search"))
                .header("Authorization", "Bearer " + adminToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() == 200) {
            JsonNode root = objectMapper.readTree(res.body());
            JsonNode results = root.path("results");
            if (results.isArray() && results.size() > 0) {
                return results.get(0).path("id").asLong();
            }
        }
        return null;
    }

    private Map<String, List<Long>> createEmployees(String adminToken, Map<String, Long> departments, List<String> logAccumulator) throws Exception {
        Map<String, List<Long>> created = new HashMap<>();

        for (Map.Entry<String, Long> entry : departments.entrySet()) {
            String deptName = entry.getKey();
            Long deptId = entry.getValue();
            List<Long> empIds = new ArrayList<>();
            created.put(deptName, empIds);

            // Create 3 employees per department
            for (int i = 1; i <= 3; i++) {
                String name = "Staff " + deptName + " " + i;
                String email = deptName.toLowerCase() + ".staff" + i + "@company.vn";
                logAccumulator.add("Creating employee '" + name + "'...");

                ObjectNode body = objectMapper.createObjectNode();
                body.put("empName", name);
                body.put("empEmail", email);
                body.put("departmentId", deptId);

                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(targetUrl + "/api/employee/create"))
                        .header("Authorization", "Bearer " + adminToken)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                        .build();

                HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
                if (res.statusCode() == 201 || res.statusCode() == 200) {
                    JsonNode root = objectMapper.readTree(res.body());
                    long id = root.path("results").path("id").asLong();
                    empIds.add(id);
                    logAccumulator.add("Employee '" + name + "' created with ID: " + id);
                } else {
                    logAccumulator.add("Warning: Employee '" + name + "' could not be created: " + res.body());
                }
            }
        }
        return created;
    }

    private Map<String, String> registerAndLinkUsers(
            String adminToken, Map<String, Long> departments, Map<String, List<Long>> employees, List<String> logAccumulator) throws Exception {
        
        Map<String, String> userTokens = new HashMap<>();
        String[] deptKeys = {"Engineering", "Marketing", "HR", "Finance"};

        for (String deptName : deptKeys) {
            List<Long> empIds = employees.get(deptName);
            if (empIds == null || empIds.isEmpty()) continue;

            Long empId = empIds.get(0); // Choose the first employee of this department to bind to a User account
            String username = "user_" + deptName.toLowerCase();
            String password = "Password123!";
            logAccumulator.add("Registering user '" + username + "'...");

            // Register
            ObjectNode regBody = objectMapper.createObjectNode();
            regBody.put("name", "User " + deptName);
            regBody.put("username", username);
            regBody.put("password", password);

            HttpRequest regReq = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl + "/api/auth/register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(regBody.toString()))
                    .build();

            HttpResponse<String> regRes = httpClient.send(regReq, HttpResponse.BodyHandlers.ofString());
            String userToken = null;
            if (regRes.statusCode() == 201 || regRes.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(regRes.body());
                userToken = root.path("results").path("token").asText();
                logAccumulator.add("User '" + username + "' registered successfully.");
            } else {
                logAccumulator.add("User '" + username + "' registration returned " + regRes.statusCode() + ". Fetching existing token via login...");
                try {
                    userToken = login(targetUrl, username, password);
                    logAccumulator.add("Logged in existing user '" + username + "'.");
                } catch (Exception loginEx) {
                    logAccumulator.add("Warning: Could not register or login user '" + username + "': " + loginEx.getMessage());
                }
            }

            if (userToken != null) {
                userTokens.put(username, userToken);

                // Fetch user ID
                HttpRequest profileReq = HttpRequest.newBuilder()
                        .uri(URI.create(targetUrl + "/api/user/me"))
                        .header("Authorization", "Bearer " + userToken)
                        .GET()
                        .build();
                HttpResponse<String> profileRes = httpClient.send(profileReq, HttpResponse.BodyHandlers.ofString());
                if (profileRes.statusCode() == 200) {
                    JsonNode root = objectMapper.readTree(profileRes.body());
                    long userId = root.path("results").path("id").asLong();

                    // Link user to employee record
                    logAccumulator.add("Linking user ID " + userId + " (username: " + username + ") to employee ID " + empId + "...");
                    ObjectNode linkBody = objectMapper.createObjectNode();
                    linkBody.put("employeeId", empId);

                    HttpRequest linkReq = HttpRequest.newBuilder()
                            .uri(URI.create(targetUrl + "/api/user/" + userId + "/employee"))
                            .header("Authorization", "Bearer " + adminToken)
                            .header("Content-Type", "application/json")
                            .PUT(HttpRequest.BodyPublishers.ofString(linkBody.toString()))
                            .build();

                    HttpResponse<String> linkRes = httpClient.send(linkReq, HttpResponse.BodyHandlers.ofString());
                    if (linkRes.statusCode() == 200) {
                        logAccumulator.add("Successfully linked user to employee.");
                    } else {
                        logAccumulator.add("Warning: Link failed: " + linkRes.body());
                    }
                }
            }
        }
        return userTokens;
    }

    private void setupAbacPolicies(String adminToken, Map<String, Long> departments, List<String> logAccumulator) throws Exception {
        logAccumulator.add("Setting up ABAC access policies...");

        // 1. Engineering: Always ALLOW (00:00 to 23:59, ALL days)
        Long engDeptId = departments.get("Engineering");
        if (engDeptId != null) {
            createOrUpdatePolicy(adminToken, "DEPARTMENT", null, "DEPARTMENT", engDeptId, "ALL", "00:00:00", "23:59:59", "ALLOW", "Engineering always allowed", logAccumulator);
        }

        // 2. Marketing: Restricted day of week (succeeds only on specific non-today days or specific windows to trigger default-deny)
        // We will configure allowance ONLY for days that are NOT today. E.g. if today is MON, we allow TUE.
        Long mktDeptId = departments.get("Marketing");
        if (mktDeptId != null) {
            DayOfWeek today = LocalDate.now().getDayOfWeek();
            DayOfWeek notToday = today == DayOfWeek.SUNDAY ? DayOfWeek.MONDAY : DayOfWeek.values()[today.ordinal() + 1];
            String notTodayStr = notToday.name().substring(0, 3); // MON, TUE...
            
            createOrUpdatePolicy(adminToken, "DEPARTMENT", null, "DEPARTMENT", mktDeptId, notTodayStr, "09:00:00", "17:00:00", "ALLOW", "Marketing allowed only on " + notTodayStr + " (triggers default-deny today)", logAccumulator);
        }

        // 3. Finance: Explicit DENY (00:00 to 23:59, ALL days)
        Long finDeptId = departments.get("Finance");
        if (finDeptId != null) {
            createOrUpdatePolicy(adminToken, "DEPARTMENT", null, "DEPARTMENT", finDeptId, "ALL", "00:00:00", "23:59:59", "DENY", "Finance explicitly denied at all times", logAccumulator);
        }

        // 4. HR: Always ALLOW (00:00 to 23:59, ALL days)
        Long hrDeptId = departments.get("HR");
        if (hrDeptId != null) {
            createOrUpdatePolicy(adminToken, "DEPARTMENT", null, "DEPARTMENT", hrDeptId, "ALL", "00:00:00", "23:59:59", "ALLOW", "HR always allowed", logAccumulator);
        }
    }

    private void createOrUpdatePolicy(
            String adminToken, String resourceType, Long resourceId, String subjectType, Long subjectId,
            String dayOfWeek, String startTime, String endTime, String effect, String description,
            List<String> logAccumulator) throws Exception {

        logAccumulator.add("Configuring policy: " + description + " (" + effect + ")...");
        
        ObjectNode body = objectMapper.createObjectNode();
        body.put("resourceType", resourceType);
        if (resourceId != null) {
            body.put("resourceId", resourceId);
        }
        body.put("subjectType", subjectType);
        body.put("subjectId", subjectId);
        body.put("dayOfWeek", dayOfWeek);
        body.put("startTime", startTime);
        body.put("endTime", endTime);
        body.put("effect", effect);
        body.put("enabled", true);
        body.put("description", description);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl + "/api/access-policies"))
                .header("Authorization", "Bearer " + adminToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() == 201 || res.statusCode() == 200) {
            logAccumulator.add("Access policy configured successfully.");
        } else {
            logAccumulator.add("Note: Access policy create returned status " + res.statusCode() + ". It may already exist.");
        }
    }
}
