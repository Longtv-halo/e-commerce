package com.demo.entity;

import com.demo.entity.abac.PolicyDayOfWeek;
import com.demo.entity.abac.PolicyEffect;
import com.demo.entity.abac.PolicySubjectType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * Represents a generic ABAC (Attribute-Based Access Control) policy.
 * <p>
 * A policy defines WHEN (time attributes) a given SUBJECT is allowed
 * or denied access to a RESOURCE.
 *
 * <p>Example:
 * <pre>
 *   resource_type = DEPARTMENT
 *   resource_id   = NULL  (all departments)
 *   subject_type  = DEPARTMENT
 *   subject_id    = 1     (IT department)
 *   day_of_week   = ALL
 *   start_time    = 08:00
 *   end_time      = 17:00
 *   effect        = ALLOW
 * </pre>
 * This policy allows everyone in the IT department to access
 * DEPARTMENT resources between 08:00 and 17:00 every day.
 */
@Entity
@Table(name = "access_policies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The type of resource being protected.
     * Examples: DEPARTMENT, SERVICE, REPORT, EMPLOYEE
     */
    @Column(name = "resource_type", nullable = false, length = 50)
    private String resourceType;

    /**
     * Specific resource ID this policy applies to.
     * NULL means this policy applies to ALL resources of the given type.
     */
    @Column(name = "resource_id")
    private Long resourceId;

    /**
     * The type of subject this policy applies to.
     * DEPARTMENT → everyone in a department
     * ROLE       → everyone with a specific role
     * USER       → a specific user
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "subject_type", nullable = false, length = 50)
    @Builder.Default
    private PolicySubjectType subjectType = PolicySubjectType.DEPARTMENT;

    /**
     * The ID of the subject (department id, role id, or user id)
     * depending on {@link #subjectType}.
     */
    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    /**
     * Day(s) of the week this policy is active.
     * ALL means every day.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    @Builder.Default
    private PolicyDayOfWeek dayOfWeek = PolicyDayOfWeek.ALL;

    /** Time from which access is allowed/denied (inclusive). */
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    /** Time until which access is allowed/denied (inclusive). */
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    /**
     * Whether this policy grants or blocks access.
     * DENY overrides ALLOW when multiple policies match.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "effect", nullable = false, length = 10)
    @Builder.Default
    private PolicyEffect effect = PolicyEffect.ALLOW;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(length = 255)
    private String description;
}
