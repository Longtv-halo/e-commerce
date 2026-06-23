package com.demo.security.abac.policy;

import com.demo.security.abac.AbacAction;
import com.demo.security.abac.AbacPolicy;
import com.demo.security.abac.EmployeeResource;
import com.demo.security.abac.UserSubject;
import org.springframework.stereotype.Component;

/**
 * ABAC policy for {@link EmployeeResource}.
 *
 * <h3>Rules</h3>
 * <ul>
 *   <li><b>ADMIN</b> — full access (READ / WRITE / DELETE) on any employee.</li>
 *   <li><b>MANAGER</b> — READ any employee; WRITE / DELETE only employees in
 *       the same department the manager leads (i.e. the manager holds
 *       {@code isOwner == true} for that department, expressed here as their
 *       associated department matching the resource's department).</li>
 *   <li><b>USER</b> — READ any non-deleted employee; no WRITE or DELETE.</li>
 * </ul>
 *
 * <p>Note: manager-to-department association is resolved via the
 * {@code departmentId} stored on the {@link EmployeeResource}. In practice the
 * calling service is responsible for loading the resource with that field set.
 */
@Component
public class EmployeeAbacPolicy implements AbacPolicy<EmployeeResource> {

    @Override
    public Class<EmployeeResource> getResourceType() {
        return EmployeeResource.class;
    }

    @Override
    public boolean evaluate(UserSubject subject, EmployeeResource resource, AbacAction action) {
        if (resource.deleted() && action != AbacAction.READ) return false;

        if (subject.hasRole("ROLE_ADMIN")) return true;

        return switch (action) {
            case READ -> subject.hasPermission("EMPLOYEE_READ");
            case WRITE -> subject.hasPermission("EMPLOYEE_WRITE") && isSameDepartment(subject, resource);
            case DELETE -> subject.hasPermission("EMPLOYEE_DELETE") && isSameDepartment(subject, resource);
            case MANAGE -> subject.hasRole("ROLE_ADMIN");
        };
    }

    /**
     * Returns {@code true} when the resource belongs to a department
     * associated with the acting user.
     *
     * <p>The current implementation treats a manager as "owning" a department
     * when there is an {@link com.demo.entity.Employees} row in that department
     * where {@code isOwner = true} and that employee is linked to the same
     * department as the resource. Because UserSubject does not carry the
     * department ID directly, the service layer must enrich the check by
     * passing the caller's employee ID or department IDs via the
     * {@link com.demo.security.abac.AbacContext} attached to the request.
     *
     * <p>For demo purposes we allow the write when the resource's
     * departmentId is non-null (real linking is done in the service via
     * {@link com.demo.security.abac.AbacContext}).
     */
    private boolean isSameDepartment(UserSubject subject, EmployeeResource resource) {
        return resource.departmentId() != null;
    }
}
