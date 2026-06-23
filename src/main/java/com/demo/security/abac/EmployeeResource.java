package com.demo.security.abac;

import com.demo.entity.Employees;

/**
 * ABAC resource wrapper for {@link Employees}.
 * <p>
 * Exposes only the attributes that policies need to make access decisions,
 * without leaking the full entity graph.
 */
public record EmployeeResource(
        Long id,
        Long departmentId,
        boolean isOwner,
        boolean deleted
) implements AbacResource {

    public static EmployeeResource of(Employees e) {
        return new EmployeeResource(
                e.getId(),
                e.getDepartment() != null ? e.getDepartment().getId() : null,
                Boolean.TRUE.equals(e.getIsOwner()),
                Boolean.TRUE.equals(e.getDeleted())
        );
    }
}
