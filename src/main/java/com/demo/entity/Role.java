package com.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique role name. Convention: ROLE_<NAME> (e.g. ROLE_ADMIN, ROLE_MANAGER).
     * Spring Security requires the "ROLE_" prefix for hasRole() checks.
     */
    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    /**
     * Permissions granted to this role.
     * Loaded eagerly so that authorities are available immediately after
     * authentication.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "role_permissions", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"))
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();
}
