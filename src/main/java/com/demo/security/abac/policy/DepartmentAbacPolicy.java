package com.demo.security.abac.policy;

import com.demo.security.abac.AbacAction;
import com.demo.security.abac.AbacPolicy;
import com.demo.security.abac.DepartmentResource;
import com.demo.security.abac.UserSubject;
import org.springframework.stereotype.Component;

/**
 * ABAC policy for {@link DepartmentResource}.
 *
 * <h3>Rules</h3>
 * <ul>
 *   <li><b>ADMIN</b> — full access on any department.</li>
 *   <li><b>MANAGER</b> — READ any department;
 *       WRITE only the department they lead (leader ID check is done by the
 *       service using {@link com.demo.security.abac.AbacContext}).</li>
 *   <li><b>USER</b> — READ only; no mutations allowed.</li>
 * </ul>
 */
@Component
public class DepartmentAbacPolicy implements AbacPolicy<DepartmentResource> {

    @Override
    public Class<DepartmentResource> getResourceType() {
        return DepartmentResource.class;
    }

    @Override
    public boolean evaluate(UserSubject subject, DepartmentResource resource, AbacAction action) {
        if (resource.deleted()) return false;

        if (subject.hasRole("ROLE_ADMIN")) return true;

        return switch (action) {
            case READ -> subject.hasPermission("DEPARTMENT_READ");
            case WRITE -> subject.hasPermission("DEPARTMENT_WRITE");
            case DELETE -> subject.hasPermission("DEPARTMENT_DELETE");
            case MANAGE -> subject.hasRole("ROLE_ADMIN");
        };
    }
}
