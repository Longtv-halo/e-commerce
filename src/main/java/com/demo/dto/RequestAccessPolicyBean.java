package com.demo.dto;

import com.demo.entity.abac.PolicyDayOfWeek;
import com.demo.entity.abac.PolicyEffect;
import com.demo.entity.abac.PolicySubjectType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestAccessPolicyBean {

    /**
     * Type of resource to protect.
     * Example values: DEPARTMENT, EMPLOYEE, REPORT
     */
    @NotBlank(message = "resourceType is required")
    private String resourceType;

    /**
     * Specific resource ID. Leave null to apply to all resources of this type.
     */
    private Long resourceId;

    /**
     * Who this policy targets: DEPARTMENT, ROLE, or USER.
     */
    @NotNull(message = "subjectType is required")
    private PolicySubjectType subjectType;

    /**
     * ID of the subject (department id / role id / user id).
     */
    @NotNull(message = "subjectId is required")
    private Long subjectId;

    /**
     * Day of week: ALL, MON, TUE, WED, THU, FRI, SAT, SUN
     */
    @NotNull(message = "dayOfWeek is required")
    private PolicyDayOfWeek dayOfWeek;

    /**
     * Start of the allowed/denied time window (HH:mm).
     */
    @NotNull(message = "startTime is required")
    private LocalTime startTime;

    /**
     * End of the allowed/denied time window (HH:mm).
     */
    @NotNull(message = "endTime is required")
    private LocalTime endTime;

    /**
     * ALLOW or DENY. Defaults to ALLOW.
     */
    @NotNull(message = "effect is required")
    private PolicyEffect effect;

    private Boolean enabled;

    private String description;
}
