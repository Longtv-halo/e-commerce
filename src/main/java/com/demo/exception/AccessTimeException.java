package com.demo.exception;

import org.springframework.security.access.AccessDeniedException;

/**
 * Thrown when an ABAC time-based policy blocks access.
 * Extends {@link AccessDeniedException} so Spring Security's
 * access-denied handler (HTTP 403) picks it up automatically.
 */
public class AccessTimeException extends AccessDeniedException {

    public AccessTimeException(String resourceType) {
        super("Outside allowed access hours for resource: " + resourceType);
    }

    public AccessTimeException(String resourceType, String detail) {
        super("Outside allowed access hours for resource: " + resourceType + ". " + detail);
    }
}
