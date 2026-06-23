package com.demo.entity.abac;

/**
 * Determines whether a matched policy grants or denies access.
 * DENY takes precedence over ALLOW (deny-overrides strategy).
 */
public enum PolicyEffect {
    ALLOW,
    DENY
}
