package com.demo.security.abac;

/**
 * Enumeration of all ABAC actions used across the system.
 * <p>
 * Decouples service code from raw strings and allows compile-time safety
 * when writing policy rules.
 */
public enum AbacAction {

    READ,
    WRITE,
    DELETE,
    MANAGE
}
