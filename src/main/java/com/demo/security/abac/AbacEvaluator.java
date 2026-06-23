package com.demo.security.abac;

import com.demo.entity.Permission;
import com.demo.entity.Role;
import com.demo.entity.Users;
import com.demo.exception.BadRequestException;
import com.demo.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Central ABAC evaluator and Spring Security
 * {@link org.springframework.security.access.PermissionEvaluator} bridge.
 *
 * <p>Dispatches access decisions to the appropriate {@link AbacPolicy} based on
 * the runtime type of the resource object.  Policies are injected automatically
 * by Spring — registering a new policy is as simple as annotating it with
 * {@code @Component}.
 *
 * <h3>Usage in {@code @PreAuthorize}</h3>
 * <pre>
 *   {@literal @}PreAuthorize("@abac.canAccess(#id, 'EmployeeResource', 'WRITE')")
 *   {@literal @}PreAuthorize("@abac.canAccessResource(#resource, 'READ')")
 * </pre>
 *
 * <h3>Usage in service code</h3>
 * <pre>{@code
 *   EmployeeResource res = EmployeeResource.of(employee);
 *   abacEvaluator.assertAccess(res, AbacAction.DELETE);
 * }</pre>
 */
@Component("abac")
@RequiredArgsConstructor
public class AbacEvaluator {

    private final List<AbacPolicy<?>> policies;
    private final UsersRepository usersRepository;

    public boolean canAccessResource(AbacResource resource, String actionName) {
        AbacAction action = AbacAction.valueOf(actionName.toUpperCase());
        UserSubject subject = currentSubject();
        return evaluate(subject, resource, action);
    }

    public boolean assertAccess(AbacResource resource, AbacAction action) {
        UserSubject subject = currentSubject();
        boolean granted = evaluate(subject, resource, action);
        if (!granted) {
            throw new BadRequestException("Access denied: you do not have permission to perform "
                    + action + " on " + resource.getClass().getSimpleName());
        }
        return true;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private boolean evaluate(UserSubject subject, AbacResource resource, AbacAction action) {
        for (AbacPolicy policy : policies) {
            if (policy.supports(resource.getClass())) {
                return policy.evaluate(subject, resource, action);
            }
        }
        return false;
    }

    private UserSubject currentSubject() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BadRequestException("No authenticated user in security context");
        }

        String username = auth.getName();
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("Authenticated user not found: " + username));

        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        Set<String> permissions = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(Permission::getName)
                .collect(Collectors.toSet());

        return new UserSubject(user.getId(), username, roles, permissions);
    }
}
