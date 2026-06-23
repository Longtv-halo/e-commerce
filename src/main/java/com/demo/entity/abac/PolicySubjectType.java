package com.demo.entity.abac;

/**
 * Defines what kind of subject (who) a policy is applied to.
 * Allows the policy engine to be extended to support different granularities:
 * - DEPARTMENT: everyone in a given department
 * - ROLE:       everyone holding a specific role
 * - USER:       a specific user
 */
public enum PolicySubjectType {
    DEPARTMENT,
    ROLE,
    USER
}
