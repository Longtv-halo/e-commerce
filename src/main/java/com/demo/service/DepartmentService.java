package com.demo.service;

import com.demo.dto.*;
import com.demo.entity.Departments;
import com.demo.entity.Employees;
import com.demo.exception.BadRequestException;
import com.demo.exception.ResourceNotFoundException;
import com.demo.repository.DepartmentEmployeeCountProjection;
import com.demo.repository.DepartmentsRepository;
import com.demo.repository.EmployeesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class DepartmentService {

    private final DepartmentsRepository departmentsRepository;
    private final EmployeesRepository employeesRepository;

    public BaseResponse<List<ResponseDepartmentBean>> getDepartmentsByNamePaging(SearchDepartmentRequest request) {
        Pageable pageable = buildPageable(request);
        Page<Departments> pageResult = departmentsRepository.findByNameLike(request.getDepartmentName(), pageable);
        return BaseResponse.ok(toResponseList(pageResult.getContent()), buildResultInfo(pageResult));
    }

    public BaseResponse<List<ResponseDepartmentBean>> getAllDepartments(BaseRequest request) {
        Pageable pageable = buildPageable(request);
        Page<Departments> pageResult = departmentsRepository.findByDeletedFalse(pageable);
        return BaseResponse.ok(toResponseList(pageResult.getContent()), buildResultInfo(pageResult));
    }

    public BaseResponse<ResponseDepartmentBean> getDepartmentById(Long id) {
        return BaseResponse.ok(toResponse(getDepartmentOrThrow(id)));
    }

    @Transactional
    public BaseResponse<ResponseDepartmentBean> createDepartment(RequestDepartmentBean request) {
        if (departmentsRepository.existsByNameIgnoreCaseAndDeletedFalse(request.getName())) {
            throw new BadRequestException("Department already exists");
        }

        Departments department = Departments.builder()
                .name(request.getName())
                .build();
        department = departmentsRepository.save(department);

        Employees leaderCandidate = Employees.builder()
                .name(request.getEmpName())
                .email(request.getEmpEmail())
                .department(department)
                .isOwner(Boolean.TRUE.equals(request.getIsOwner()))
                .build();
        leaderCandidate = employeesRepository.save(leaderCandidate);

        if (Boolean.TRUE.equals(request.getIsOwner())) {
            department.setLeader(leaderCandidate);
            department = departmentsRepository.save(department);
        }

        return BaseResponse.created(toResponse(department));
    }

    @Transactional
    public BaseResponse<ResponseDepartmentBean> updateDepartment(Long id, RequestDepartmentBean request) {
        Departments department = getDepartmentOrThrow(id);

        if (department == null) {
            throw new BadRequestException("Department not found");
        }

        Optional<Employees> employee = employeesRepository.findByEmailAndDeletedFalse(request.getEmpEmail());

        if (employee.isEmpty()) {
            throw new BadRequestException("Employee with email " + request.getEmpEmail() + " not found");
        }

        if (department.getLeader().getId() == null
                || !department.getLeader().getId().equals(employee.get().getId())
                || !Boolean.TRUE.equals(employee.get().getIsOwner())) {
            throw new BadRequestException("Employee is not the leader of the department");
        }

        department = departmentsRepository.save(department);
        return BaseResponse.created(toResponse(department));
    }

    @Transactional
    public BaseResponse<Void> deleteDepartment(Long id) {
        Departments department = getDepartmentOrThrow(id);
        long employeeCount = employeesRepository.countByDepartmentIdAndDeletedFalse(id);
        if (employeeCount > 0) {
            throw new ResourceNotFoundException("Department with id " + id + " cannot be deleted because it has " + employeeCount + " employee(s)");
        }

        department.setDeleted(true);
        departmentsRepository.save(department);
        return BaseResponse.<Void>builder()
                .success(true)
                .build();
    }

    private Pageable buildPageable(BaseRequest request) {
        Sort sort = "desc".equalsIgnoreCase(request.getSortDir())
                ? Sort.by(request.getSortBy()).descending()
                : Sort.by(request.getSortBy()).ascending();

        return PageRequest.of(request.getPage(), request.getSize(), sort);
    }

    private ResultInfo buildResultInfo(Page<?> pageResult) {
        return ResultInfo.builder()
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .hasNext(pageResult.hasNext())
                .hasPrevious(pageResult.hasPrevious())
                .build();
    }

    private Departments getDepartmentOrThrow(Long id) {
        return departmentsRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department with id " + id + " not found"));
    }

    private List<ResponseDepartmentBean> toResponseList(List<Departments> departments) {
        Map<Long, Long> employeeCountMap = employeesRepository.countEmployeesByDepartment().stream()
                .collect(Collectors.toMap(
                        DepartmentEmployeeCountProjection::getDepartmentId,
                        DepartmentEmployeeCountProjection::getEmployeeCount
                ));

        return departments.stream()
                .map(department -> toResponse(department, employeeCountMap))
                .collect(Collectors.toList());
    }

    private ResponseDepartmentBean toResponse(Departments department) {
        Long employeeCount = employeesRepository.countByDepartmentIdAndDeletedFalse(department.getId());

        return ResponseDepartmentBean.builder()
                .id(department.getId())
                .name(department.getName())
                .employeeCount(employeeCount)
                .build();
    }

    private ResponseDepartmentBean toResponse(Departments department, Map<Long, Long> employeeCountMap) {
        Long employeeCount = employeeCountMap.getOrDefault(department.getId(), 0L);

        return ResponseDepartmentBean.builder()
                .id(department.getId())
                .name(department.getName())
                .employeeCount(employeeCount)
                .build();
    }

    @Transactional
    public BaseResponse<ResponseDepartmentBean> changeRoleLeader(Long id, RequestAssignRoleDepartmentBean request) {
        Departments department = getDepartmentOrThrow(id);

        if (department == null) {
            throw new BadRequestException("Department not found");
        }

        Optional<Employees> oldOwner = employeesRepository.findByEmailAndDeletedFalse(request.getOwnerEmail());
        Optional<Employees> newOwner = employeesRepository.findByEmailAndDeletedFalse(request.getNewOwnerEmail());

        if (oldOwner.isEmpty()) {
            throw new BadRequestException("Employee with email " + request.getOwnerEmail() + " not found");
        }

        if (newOwner.isEmpty()) {
            throw new BadRequestException("Employee be assigned leader with email "
                    + request.getNewOwnerEmail() + " not found");
        }

        if (department.getLeader().getId() == null
                || !department.getLeader().getId().equals(oldOwner.get().getId())
                || !Boolean.TRUE.equals(oldOwner.get().getIsOwner())) {
            throw new BadRequestException("Employee is not the leader of the department");
        }

        if (Boolean.TRUE.equals(newOwner.get().getIsOwner())) {
            throw new BadRequestException("Employee is the leader of the department and cannot be assigned as a new leader");
        }

        if (!department.getId().equals(newOwner.get().getDepartment().getId())) {
            throw new BadRequestException("Employee is not in the department and cannot be assigned as a new leader");
        }

        oldOwner.get().setIsOwner(false);
        newOwner.get().setIsOwner(true);
        employeesRepository.save(oldOwner.get());
        employeesRepository.save(newOwner.get());

        department.setLeader(newOwner.get());
        department = departmentsRepository.save(department);
        return BaseResponse.created(toResponse(department));
    }

}