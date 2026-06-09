package com.demo.security.rbac;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;

/**
 * Central RBAC evaluator.
 * <p>
 * Exposes fine-grained helper methods that can be used either programmatically
 * inside services/controllers, or via SpEL in {@code @PreAuthorize} /
 * {@code @PostAuthorize} annotations.
 * <p>
 * Usage in annotation:
 * <pre>
 *   {@literal @}PreAuthorize("@rbac.hasPermission('EMPLOYEE_WRITE')")
 *   {@literal @}PreAuthorize("@rbac.hasAnyPermission('EMPLOYEE_READ','DEPARTMENT_READ')")
 *   {@literal @}PreAuthorize("@rbac.hasRole('ROLE_ADMIN')")
 * </pre>
 */
@Component("rbac")
public class RbacEvaluator {

    public boolean hasPermission(String permission) {
        return hasAuthority(permission);
    }

    public boolean hasAnyPermission(String... permissions) {
        return Arrays.stream(permissions).anyMatch(this::hasAuthority);
    }

    public boolean hasAllPermissions(String... permissions) {
        return Arrays.stream(permissions).allMatch(this::hasAuthority);
    }

    public boolean hasRole(String role) {
        return hasAuthority(role);
    }

    public boolean hasAnyRole(String... roles) {
        return Arrays.stream(roles).anyMatch(this::hasAuthority);
    }

    private boolean hasAuthority(String authority) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;
        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        return authorities.stream().anyMatch(a -> a.getAuthority().equals(authority));
    }
}
