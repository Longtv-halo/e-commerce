package com.demo.repository;

import com.demo.entity.Departments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentsRepository extends JpaRepository<Departments, Long> {

	@Query("SELECT d FROM Departments d WHERE d.deleted = false AND d.name LIKE %:keyword%")
	Page<Departments> findByNameLike(@Param("keyword") String keyword, Pageable pageable);

	@Query("SELECT d FROM Departments d WHERE d.deleted = false")
	Page<Departments> findByDeletedFalse(Pageable pageable);

	Optional<Departments> findByIdAndDeletedFalse(Long id);

	boolean existsByNameIgnoreCaseAndDeletedFalse(String name);
}

