package com.demo.controller;

import com.demo.dto.BaseResponse;
import com.demo.dto.RequestAccessPolicyBean;
import com.demo.dto.ResponseAccessPolicyBean;
import com.demo.service.AccessPolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for managing ABAC (Attribute-Based Access Control) policies.
 *
 * <p>All endpoints require the {@code POLICY_MANAGE} permission.
 *
 * <pre>
 *   POST   /api/access-policies                             – create a policy
 *   GET    /api/access-policies                             – list all policies
 *   GET    /api/access-policies?resourceType=DEPARTMENT     – filter by resource type
 *   GET    /api/access-policies/{id}                        – get one policy
 *   PUT    /api/access-policies/{id}                        – update a policy
 *   DELETE /api/access-policies/{id}                        – delete a policy
 * </pre>
 */
@RestController
@RequestMapping("/api/access-policies")
@RequiredArgsConstructor
public class AccessPolicyController {

    private final AccessPolicyService accessPolicyService;

    /**
     * Create a new ABAC access policy.
     *
     * <p>Example request body:
     * <pre>
     * {
     *   "resourceType": "DEPARTMENT",
     *   "resourceId": null,
     *   "subjectType": "DEPARTMENT",
     *   "subjectId": 1,
     *   "dayOfWeek": "ALL",
     *   "startTime": "08:00:00",
     *   "endTime": "17:00:00",
     *   "effect": "ALLOW",
     *   "enabled": true,
     *   "description": "IT dept working hours"
     * }
     * </pre>
     */
    @PostMapping
    @PreAuthorize("hasAuthority('POLICY_MANAGE')")
    public ResponseEntity<BaseResponse<ResponseAccessPolicyBean>> create(
            @Valid @RequestBody RequestAccessPolicyBean request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accessPolicyService.create(request));
    }

    /**
     * List all policies, optionally filtered by {@code resourceType}.
     *
     * @param resourceType optional filter (e.g. DEPARTMENT, EMPLOYEE)
     */
    @GetMapping
    @PreAuthorize("hasAuthority('POLICY_MANAGE')")
    public ResponseEntity<BaseResponse<List<ResponseAccessPolicyBean>>> getAll(
            @RequestParam(required = false) String resourceType) {
        if (resourceType != null && !resourceType.isBlank()) {
            return ResponseEntity.ok(accessPolicyService.getByResourceType(resourceType.toUpperCase()));
        }
        return ResponseEntity.ok(accessPolicyService.getAll());
    }

    /**
     * Get a single policy by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('POLICY_MANAGE')")
    public ResponseEntity<BaseResponse<ResponseAccessPolicyBean>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(accessPolicyService.getById(id));
    }

    /**
     * Update an existing policy.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('POLICY_MANAGE')")
    public ResponseEntity<BaseResponse<ResponseAccessPolicyBean>> update(
            @PathVariable Long id,
            @Valid @RequestBody RequestAccessPolicyBean request) {
        return ResponseEntity.ok(accessPolicyService.update(id, request));
    }

    /**
     * Delete a policy.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('POLICY_MANAGE')")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(accessPolicyService.delete(id));
    }
}
