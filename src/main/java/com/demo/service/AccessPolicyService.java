package com.demo.service;

import com.demo.dto.RequestAccessPolicyBean;
import com.demo.dto.ResponseAccessPolicyBean;
import com.demo.dto.BaseResponse;
import com.demo.entity.AccessPolicy;
import com.demo.exception.ResourceNotFoundException;
import com.demo.repository.AccessPolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages CRUD operations for ABAC {@link AccessPolicy} records.
 *
 * <p>These policies define time-based access rules for resources and are
 * evaluated at runtime by {@link com.demo.security.AbacPolicyEvaluator}.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class AccessPolicyService {

    private final AccessPolicyRepository accessPolicyRepository;

    // ── Queries ──────────────────────────────────────────────────────────────

    public BaseResponse<List<ResponseAccessPolicyBean>> getAll() {
        List<ResponseAccessPolicyBean> results = accessPolicyRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return BaseResponse.ok(results);
    }

    public BaseResponse<List<ResponseAccessPolicyBean>> getByResourceType(String resourceType) {
        List<ResponseAccessPolicyBean> results =
                accessPolicyRepository.findByResourceTypeAndEnabled(resourceType, true)
                        .stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList());
        return BaseResponse.ok(results);
    }

    public BaseResponse<ResponseAccessPolicyBean> getById(Long id) {
        return BaseResponse.ok(toResponse(findOrThrow(id)));
    }

    // ── Mutations ────────────────────────────────────────────────────────────

    @Transactional
    public BaseResponse<ResponseAccessPolicyBean> create(RequestAccessPolicyBean request) {
        AccessPolicy policy = AccessPolicy.builder()
                .resourceType(request.getResourceType().toUpperCase())
                .resourceId(request.getResourceId())
                .subjectType(request.getSubjectType())
                .subjectId(request.getSubjectId())
                .dayOfWeek(request.getDayOfWeek())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .effect(request.getEffect())
                .enabled(request.getEnabled() != null ? request.getEnabled() : Boolean.TRUE)
                .description(request.getDescription())
                .build();

        policy = accessPolicyRepository.save(policy);
        log.info("Created ABAC policy id={} type={} subject={}:{} day={} {}–{}",
                policy.getId(), policy.getResourceType(),
                policy.getSubjectType(), policy.getSubjectId(),
                policy.getDayOfWeek(), policy.getStartTime(), policy.getEndTime());

        return BaseResponse.created(toResponse(policy));
    }

    @Transactional
    public BaseResponse<ResponseAccessPolicyBean> update(Long id, RequestAccessPolicyBean request) {
        AccessPolicy policy = findOrThrow(id);

        policy.setResourceType(request.getResourceType().toUpperCase());
        policy.setResourceId(request.getResourceId());
        policy.setSubjectType(request.getSubjectType());
        policy.setSubjectId(request.getSubjectId());
        policy.setDayOfWeek(request.getDayOfWeek());
        policy.setStartTime(request.getStartTime());
        policy.setEndTime(request.getEndTime());
        policy.setEffect(request.getEffect());
        if (request.getEnabled() != null) {
            policy.setEnabled(request.getEnabled());
        }
        policy.setDescription(request.getDescription());

        policy = accessPolicyRepository.save(policy);
        log.info("Updated ABAC policy id={}", policy.getId());
        return BaseResponse.ok(toResponse(policy));
    }

    @Transactional
    public BaseResponse<Void> delete(Long id) {
        AccessPolicy policy = findOrThrow(id);
        accessPolicyRepository.delete(policy);
        log.info("Deleted ABAC policy id={}", id);
        return BaseResponse.<Void>builder().success(true).build();
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private AccessPolicy findOrThrow(Long id) {
        return accessPolicyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AccessPolicy with id " + id + " not found"));
    }

    private ResponseAccessPolicyBean toResponse(AccessPolicy policy) {
        return ResponseAccessPolicyBean.builder()
                .id(policy.getId())
                .resourceType(policy.getResourceType())
                .resourceId(policy.getResourceId())
                .subjectType(policy.getSubjectType())
                .subjectId(policy.getSubjectId())
                .dayOfWeek(policy.getDayOfWeek())
                .startTime(policy.getStartTime())
                .endTime(policy.getEndTime())
                .effect(policy.getEffect())
                .enabled(policy.getEnabled())
                .description(policy.getDescription())
                .build();
    }
}
