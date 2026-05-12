package com.demo.repository;

import com.demo.entity.Employees;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeesRepository extends JpaRepository<Employees, Long> {

  @Query("SELECT e FROM Employees e WHERE e.deleted = false AND e.name LIKE %:keyword%")
    Page<Employees> findByNameLike(@Param("keyword") String keyword, Pageable pageable);

  @Query("SELECT e FROM Employees e WHERE e.deleted = false")
  Page<Employees> findByDeletedFalse(Pageable pageable);

  Optional<Employees> findByIdAndDeletedFalse(Long id);

  boolean existsByEmailIgnoreCaseAndDeletedFalse(String email);

  long countByDepartmentIdAndDeletedFalse(Long departmentId);

  @Query("SELECT e.department.id AS departmentId, COUNT(e) AS employeeCount FROM Employees e WHERE e.deleted = false GROUP BY e.department.id")
    List<DepartmentEmployeeCountProjection> countEmployeesByDepartment();
}
