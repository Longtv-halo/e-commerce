package com.demo.security.abac;

import com.demo.entity.Users;

/**
 * Immutable snapshot of the authenticated user's relevant attributes
 * used during an ABAC policy decision.
 *
 * <p>Keeps policies decoupled from the Spring Security {@link org.springframework.security.core.Authentication}
 * object and from the full {@link Users} entity.
 *
 * @param id          Database PK of the user.
 * @param username    Login name (also the JWT subject).
 * @param roles       Set of role names the user holds (e.g. "ROLE_ADMIN").
 * @param permissions Flat set of all permission names derived from all roles.
 */
public record UserSubject(
        Long id,
        String username,
        java.util.Set<String> roles,
        java.util.Set<String> permissions
) {
    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }
}
