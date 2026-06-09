package com.demo.security.abac;

import com.demo.entity.Departments;

/**
 * ABAC resource wrapper for {@link Departments}.
 * <p>
 * Exposes only the attributes required for policy evaluation.
 */
public record DepartmentResource(
        Long id,
        Long leaderId,
        boolean deleted
) implements AbacResource {

    public static DepartmentResource of(Departments d) {
        return new DepartmentResource(
                d.getId(),
                d.getLeader() != null ? d.getLeader().getId() : null,
                Boolean.TRUE.equals(d.getDeleted())
        );
    }
}
