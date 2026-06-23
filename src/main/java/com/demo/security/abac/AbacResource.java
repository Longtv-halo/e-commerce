package com.demo.security.abac;

/**
 * Marker interface for any domain object that can be used as an ABAC resource.
 * <p>
 * All entities that participate in ABAC decisions (Employee, Department, etc.)
 * should implement this interface so they can be passed uniformly to
 * {@link AbacEvaluator}.
 */
public interface AbacResource {
}
