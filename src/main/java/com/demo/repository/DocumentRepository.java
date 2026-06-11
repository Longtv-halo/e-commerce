package com.demo.repository;

import com.demo.entity.Documents;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Documents, Long> {
    Optional<Documents> findByObjectName(String objectName);

    List<Documents> findByUploaderId(UUID uploaderId);

    List<Documents> findByUploaderUsername(String uploaderUsername);

    boolean existsByObjectName(String objectName);

    void deleteByObjectName(String objectName);

    Optional<Documents> findByObjectNameAndUploaderId(String objectName, UUID uploaderId);
}