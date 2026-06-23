package com.demo.security.abac;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * Thread-scoped context bag carrying extra attributes needed during an ABAC
 * evaluation that cannot be derived solely from the JWT / {@link UserSubject}.
 *
 * <p>Typical use-cases:
 * <ul>
 *   <li>Store the caller's {@code employeeId} after resolving the user → employee
 *       link, so the policy can compare it with the resource's owner.</li>
 *   <li>Store the caller's {@code departmentId} so the policy can enforce
 *       "same-department" constraints.</li>
 * </ul>
 *
 * <p>An instance is created per-request by {@link AbacContextHolder} and
 * discarded after the request completes.
 */
@Getter
@Setter
@RequiredArgsConstructor
public class AbacContext {

    /** Identifies the employee record linked to the authenticated user (nullable). */
    private Long callerEmployeeId;

    /** The department the caller currently leads / belongs to (nullable). */
    private Long callerDepartmentId;

    /** Generic extension bag for domain-specific attributes. */
    private final Map<String, Object> attributes = new HashMap<>();

    public void put(String key, Object value) {
        attributes.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) attributes.get(key);
    }
}
