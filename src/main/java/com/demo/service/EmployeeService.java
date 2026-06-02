package com.demo.service;

import com.demo.dto.BaseRequest;
import com.demo.dto.BaseResponse;
import com.demo.dto.RequestEmployeeBean;
import com.demo.dto.ResponseEmployeeBean;
import com.demo.dto.SearchEmployeeRequest;
import com.demo.dto.ResultInfo;
import com.demo.entity.Departments;
import com.demo.entity.Employees;
import com.demo.exception.BadRequestException;
import com.demo.exception.ResourceNotFoundException;
import com.demo.repository.DepartmentsRepository;
import com.demo.repository.EmployeesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

    private final EmployeesRepository employeeRepository;
    private final DepartmentsRepository departmentsRepository;

    public BaseResponse<List<ResponseEmployeeBean>> getEmployeesByNamePaging(SearchEmployeeRequest request) {
        Pageable pageable = buildPageable(request);
        Page<Employees> pageResult = employeeRepository.findByNameLike(request.getEmpName(), pageable);

        return BaseResponse.ok(toResponseList(pageResult.getContent()), buildResultInfo(pageResult));
    }

    public BaseResponse<List<ResponseEmployeeBean>> getAllEmployees(BaseRequest request) {
        Pageable pageable = buildPageable(request);
        Page<Employees> pageResult = employeeRepository.findByDeletedFalse(pageable);

        return BaseResponse.ok(toResponseList(pageResult.getContent()), buildResultInfo(pageResult));
    }

    public BaseResponse<ResponseEmployeeBean> getEmployeeById(Long id) {
        Employees employee = getEmployeeOrThrow(id);

        return BaseResponse.ok(toResponse(employee));
    }

    @Transactional
    public BaseResponse<ResponseEmployeeBean> createEmployees(RequestEmployeeBean request,
                                                              boolean isOwner) {
        if (employeeRepository.existsByEmailIgnoreCaseAndDeletedFalse(request.getEmpEmail())) {
            throw new BadRequestException("Employee email already exists");
        }

        Departments department = getDepartmentOrThrow(request.getDepartmentId());

        if (department.getLeader().getId() == null) {
            throw new BadRequestException("Please specify leader id");
        }

        Employees employee = Employees.builder()
                .name(request.getEmpName())
                .email(request.getEmpEmail())
                .department(department)
                .isOwner(isOwner)
                .build();
        Employees savedEmployee = employeeRepository.save(employee);

        return BaseResponse.created(toResponse(savedEmployee));
    }

    @Transactional
    public BaseResponse<ResponseEmployeeBean> updateEmployees(Long id, RequestEmployeeBean request) {
        Employees employee = getEmployeeOrThrow(id);
        Departments department = getDepartmentOrThrow(request.getDepartmentId());

        employee.setName(request.getEmpName());
        employee.setEmail(request.getEmpEmail());
        employee.setDepartment(department);
        Employees savedEmployee = employeeRepository.save(employee);

        return BaseResponse.ok(toResponse(savedEmployee));
    }

    @Transactional
    public BaseResponse<Void> deleteEmployees(Long id) {
        Employees employee = getEmployeeOrThrow(id);
        employee.setDeleted(true);
        employeeRepository.save(employee);

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

    private Employees getEmployeeOrThrow(Long id) {
        return employeeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee with id " + id + " not found"));
    }

    private Departments getDepartmentOrThrow(Long id) {
        return departmentsRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department with id " + id + " not found"));
    }

    private List<ResponseEmployeeBean> toResponseList(List<Employees> employees) {
        return employees.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ResponseEmployeeBean toResponse(Employees employee) {
        Departments department = employee.getDepartment();

        return ResponseEmployeeBean.builder()
                .id(employee.getId())
                .empName(employee.getName())
                .empEmail(employee.getEmail())
                .departmentId(department != null ? department.getId() : null)
                .departmentName(department != null ? department.getName() : null)
                .build();
    }
}
