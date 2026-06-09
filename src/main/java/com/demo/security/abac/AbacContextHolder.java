package com.demo.security.abac;

/**
 * Provides thread-local access to the current request's {@link AbacContext}.
 *
 * <p>The context is set once (typically in the JWT filter or a request-scoped
 * bean) and cleaned up in a {@code finally} block or servlet filter to prevent
 * memory leaks.
 *
 * <h3>Lifecycle example</h3>
 * <pre>{@code
 * // In JwtAuthenticationFilter (after successful authentication):
 * AbacContextHolder.set(new AbacContext());
 *
 * // In service layer:
 * AbacContext ctx = AbacContextHolder.get();
 * ctx.setCallerDepartmentId(resolvedDepartmentId);
 *
 * // After request:
 * AbacContextHolder.clear();
 * }</pre>
 */
public final class AbacContextHolder {

    private static final ThreadLocal<AbacContext> HOLDER = new ThreadLocal<>();

    private AbacContextHolder() {}

    public static void set(AbacContext ctx) {
        HOLDER.set(ctx);
    }

    public static AbacContext get() {
        AbacContext ctx = HOLDER.get();
        if (ctx == null) {
            ctx = new AbacContext();
            HOLDER.set(ctx);
        }
        return ctx;
    }

    public static void clear() {
        HOLDER.remove();
    }
}
