package com.demo.security;

import com.demo.entity.AccessPolicy;
import com.demo.entity.Users;
import com.demo.entity.abac.PolicyDayOfWeek;
import com.demo.entity.abac.PolicyEffect;
import com.demo.entity.abac.PolicySubjectType;
import com.demo.exception.AccessTimeException;
import com.demo.repository.AccessPolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * ABAC (Attribute-Based Access Control) policy evaluator.
 *
 * <p>Evaluates time-based access policies for a given resource type.
 * This bean is referenced in {@code @PreAuthorize} expressions via its
 * Spring bean name {@code abacEval}:
 *
 * <pre>
 *   @PreAuthorize("hasAuthority('EMPLOYEE_READ') and @abacEval.isAllowed(authentication, 'DEPARTMENT', null)")
 * </pre>
 *
 * <h3>Policy evaluation algorithm (deny-overrides)</h3>
 * <ol>
 *   <li>Resolve the current user's department (subject).</li>
 *   <li>If the user has no department → ALLOW (no constraint).</li>
 *   <li>Load all enabled policies matching (resourceType, resourceId, DEPARTMENT, departmentId).</li>
 *   <li>If no policies exist → ALLOW (no restriction configured).</li>
 *   <li>For each matching policy, evaluate day-of-week and time window:
 *       <ul>
 *         <li>Any DENY match → immediately throw {@link AccessTimeException}.</li>
 *         <li>At least one ALLOW match → grant access.</li>
 *       </ul>
 *   </li>
 *   <li>If policies exist but none match current time → DENY (default-deny when policies are present).</li>
 * </ol>
 */
@Component("abacEval")
@RequiredArgsConstructor
@Slf4j
public class AbacPolicyEvaluator {

    private final AccessPolicyRepository accessPolicyRepository;

    /**
     * Checks whether the authenticated user is allowed to access a resource
     * at the current date and time.
     *
     * @param authentication the current Spring Security authentication
     * @param resourceType   e.g. "DEPARTMENT", "EMPLOYEE", "REPORT"
     * @param resourceId     specific resource id, or {@code null} to check against wildcard policies
     * @return {@code true} if access is permitted
     * @throws AccessTimeException if an explicit DENY policy matches
     */
    public boolean isAllowed(Authentication authentication, String resourceType, Long resourceId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof Users user)) {
            // Non-Users principal (e.g. anonymous) — deny
            return false;
        }

        // ── Resolve subject (department) ────────────────────────────────────
        Long departmentId = resolveDepartmentId(user);

        if (departmentId == null) {
            // User has no department (e.g. system admin without employee record)
            // No department-level time policy can apply → allow
            log.debug("ABAC: user '{}' has no department, skipping time check for {}",
                    user.getUsername(), resourceType);
            return true;
        }

        // ── Load applicable policies ─────────────────────────────────────────
        List<AccessPolicy> policies = resourceId != null
                ? accessPolicyRepository.findApplicablePolicies(
                        resourceType, resourceId, PolicySubjectType.DEPARTMENT, departmentId)
                : accessPolicyRepository.findApplicablePoliciesByType(
                        resourceType, PolicySubjectType.DEPARTMENT, departmentId);

        if (policies.isEmpty()) {
            // No policies configured for this department/resource → no restriction
            log.debug("ABAC: no policies for dept={} resource={}:{}, allowing",
                    departmentId, resourceType, resourceId);
            return true;
        }

        // ── Evaluate against current time ────────────────────────────────────
        LocalDateTime now = LocalDateTime.now();
        LocalTime     currentTime = now.toLocalTime();
        DayOfWeek     currentDay  = now.getDayOfWeek();

        boolean anyAllow = false;

        for (AccessPolicy policy : policies) {
            if (!matchesDay(policy.getDayOfWeek(), currentDay)) {
                continue;
            }
            if (!matchesTime(policy.getStartTime(), policy.getEndTime(), currentTime)) {
                continue;
            }

            // Policy matches current time window
            if (policy.getEffect() == PolicyEffect.DENY) {
                log.warn("ABAC DENY: user='{}' dept={} resource={}:{} time={} policy={}",
                        user.getUsername(), departmentId, resourceType, resourceId, currentTime, policy.getId());
                throw new AccessTimeException(resourceType,
                        "Denied by policy for your department at this time.");
            }

            anyAllow = true;
        }

        if (!anyAllow) {
            // Policies exist but none matched the current time window → default deny
            log.warn("ABAC DEFAULT-DENY: user='{}' dept={} resource={}:{} time={}",
                    user.getUsername(), departmentId, resourceType, resourceId, currentTime);
            throw new AccessTimeException(resourceType,
                    "No active access window found for your department at this time.");
        }

        log.debug("ABAC ALLOW: user='{}' dept={} resource={}:{} time={}",
                user.getUsername(), departmentId, resourceType, resourceId, currentTime);
        return true;
    }

    // ────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Resolves the department ID from the user's linked employee record.
     *
     * @return department id, or {@code null} if the user has no employee / department
     */
    private Long resolveDepartmentId(Users user) {
        if (user.getEmployee() == null) return null;
        if (user.getEmployee().getDepartment() == null) return null;
        return user.getEmployee().getDepartment().getId();
    }

    /**
     * Checks whether the current day-of-week matches the policy's configured day.
     */
    private boolean matchesDay(PolicyDayOfWeek policyDay, DayOfWeek currentDay) {
        if (policyDay == PolicyDayOfWeek.ALL) return true;
        return switch (currentDay) {
            case MONDAY    -> policyDay == PolicyDayOfWeek.MON;
            case TUESDAY   -> policyDay == PolicyDayOfWeek.TUE;
            case WEDNESDAY -> policyDay == PolicyDayOfWeek.WED;
            case THURSDAY  -> policyDay == PolicyDayOfWeek.THU;
            case FRIDAY    -> policyDay == PolicyDayOfWeek.FRI;
            case SATURDAY  -> policyDay == PolicyDayOfWeek.SAT;
            case SUNDAY    -> policyDay == PolicyDayOfWeek.SUN;
        };
    }

    /**
     * Checks whether the current time falls within [startTime, endTime] (inclusive).
     */
    private boolean matchesTime(LocalTime start, LocalTime end, LocalTime current) {
        return !current.isBefore(start) && !current.isAfter(end);
    }
}
