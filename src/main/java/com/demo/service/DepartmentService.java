package com.demo.service;

import com.demo.dto.BaseRequest;
import com.demo.dto.BaseResponse;
import com.demo.dto.RequestDepartmentBean;
import com.demo.dto.ResponseDepartmentBean;
import com.demo.dto.ResultInfo;
import com.demo.dto.SearchDepartmentRequest;
import com.demo.entity.Departments;
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

        Departments savedDepartment = departmentsRepository.save(department);
        return BaseResponse.created(toResponse(savedDepartment));
    }

    @Transactional
    public BaseResponse<ResponseDepartmentBean> updateDepartment(Long id, RequestDepartmentBean request) {
        Departments department = getDepartmentOrThrow(id);
        department.setName(request.getName());

        Departments savedDepartment = departmentsRepository.save(department);
        return BaseResponse.ok(toResponse(savedDepartment));
    }

    @Transactional
    public BaseResponse<Void> deleteDepartment(Long id) {
        Departments department = getDepartmentOrThrow(id);
        long employeeCount = employeesRepository.countByDepartment_IdAndDeletedFalse(id);
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
        Long employeeCount = employeesRepository.countByDepartment_IdAndDeletedFalse(department.getId());

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
}


