package com.demo.security.abac;

/**
 * SPI for a single ABAC policy rule.
 *
 * <p>Each policy is responsible for one cohesive set of access decisions on one
 * resource type. Policies are collected and invoked by {@link AbacEvaluator}.
 *
 * @param <R> the concrete {@link AbacResource} type this policy handles
 */
public interface AbacPolicy<R extends AbacResource> {

    /**
     * Returns {@code true} if this policy applies to the given resource type.
     * The default implementation uses {@link Class#isAssignableFrom}.
     */
    default boolean supports(Class<? extends AbacResource> resourceType) {
        return getResourceType().isAssignableFrom(resourceType);
    }

    /** The concrete resource class this policy handles. */
    Class<R> getResourceType();

    /**
     * Evaluates access for {@code subject} on {@code resource} for the given {@code action}.
     *
     * @return {@code true} if access is granted, {@code false} otherwise
     */
    boolean evaluate(UserSubject subject, R resource, AbacAction action);
}
