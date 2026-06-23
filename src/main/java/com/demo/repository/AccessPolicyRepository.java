package com.demo.repository;

import com.demo.entity.AccessPolicy;
import com.demo.entity.abac.PolicySubjectType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for querying ABAC {@link AccessPolicy} rules.
 */
@Repository
public interface AccessPolicyRepository extends JpaRepository<AccessPolicy, Long> {

    /**
     * Load all enabled policies for a given resource type and subject.
     * Includes both resource-specific (resource_id = :resourceId)
     * and wildcard policies (resource_id IS NULL).
     */
    @Query("""
        SELECT p FROM AccessPolicy p
         WHERE p.enabled   = true
           AND p.resourceType  = :resourceType
           AND p.subjectType   = :subjectType
           AND p.subjectId     = :subjectId
           AND (p.resourceId IS NULL OR p.resourceId = :resourceId)
    """)
    List<AccessPolicy> findApplicablePolicies(
            @Param("resourceType")  String            resourceType,
            @Param("resourceId")    Long              resourceId,
            @Param("subjectType")   PolicySubjectType subjectType,
            @Param("subjectId")     Long              subjectId
    );

    /**
     * Load all enabled policies for a given resource type, without filtering
     * by resource_id (used when resourceId is unknown at evaluation time).
     */
    @Query("""
        SELECT p FROM AccessPolicy p
         WHERE p.enabled      = true
           AND p.resourceType = :resourceType
           AND p.subjectType  = :subjectType
           AND p.subjectId    = :subjectId
    """)
    List<AccessPolicy> findApplicablePoliciesByType(
            @Param("resourceType") String            resourceType,
            @Param("subjectType")  PolicySubjectType subjectType,
            @Param("subjectId")    Long              subjectId
    );

    /** Find all policies for admin management listing. */
    List<AccessPolicy> findByResourceTypeAndEnabled(String resourceType, Boolean enabled);

    List<AccessPolicy> findAllByEnabled(Boolean enabled);
}
