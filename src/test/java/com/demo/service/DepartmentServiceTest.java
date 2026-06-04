package com.demo.service;

import com.demo.dto.BaseRequest;
import com.demo.dto.BaseResponse;
import com.demo.dto.RequestDepartmentBean;
import com.demo.dto.ResponseDepartmentBean;
import com.demo.dto.SearchDepartmentRequest;
import com.demo.entity.Departments;
import com.demo.exception.BadRequestException;
import com.demo.exception.ResourceNotFoundException;
import com.demo.repository.DepartmentsRepository;
import com.demo.repository.EmployeesRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentsRepository departmentsRepository;

    @Mock
    private EmployeesRepository employeesRepository;

    @InjectMocks
    private DepartmentService departmentService;

    @Test
    void createDepartment_shouldReturnCreatedDepartment() {
        RequestDepartmentBean request = new RequestDepartmentBean();
        request.setName("IT");

        Departments saved = Departments.builder()
                .id(1L)
                .name("IT")
                .build();

        when(departmentsRepository.existsByNameIgnoreCaseAndDeletedFalse("IT")).thenReturn(false);
        when(departmentsRepository.save(any(Departments.class))).thenReturn(saved);
        when(employeesRepository.countByDepartmentIdAndDeletedFalse(1L)).thenReturn(0L);

        BaseResponse<ResponseDepartmentBean> response = departmentService.createDepartment(request);

        assertTrue(response.isSuccess());
        assertEquals(1L, response.getResults().getId());
        assertEquals("IT", response.getResults().getName());
        assertEquals(0L, response.getResults().getEmployeeCount());
        verify(departmentsRepository).save(any(Departments.class));
    }

    @Test
    void createDepartment_shouldThrowWhenDepartmentAlreadyExists() {
        RequestDepartmentBean request = new RequestDepartmentBean();
        request.setName("IT");

        when(departmentsRepository.existsByNameIgnoreCaseAndDeletedFalse("IT")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> departmentService.createDepartment(request));
    }

    @Test
    void getDepartmentById_shouldReturnDepartment() {
        Departments department = Departments.builder()
                .id(2L)
                .name("HR")
                .build();

        when(departmentsRepository.findByIdAndDeletedFalse(2L)).thenReturn(Optional.of(department));
        when(employeesRepository.countByDepartmentIdAndDeletedFalse(2L)).thenReturn(3L);

        BaseResponse<ResponseDepartmentBean> response = departmentService.getDepartmentById(2L);

        assertTrue(response.isSuccess());
        assertEquals("HR", response.getResults().getName());
        assertEquals(3L, response.getResults().getEmployeeCount());
    }

    @Test
    void updateDepartment_shouldReturnUpdatedDepartment() {
        Departments department = Departments.builder()
                .id(3L)
                .name("Sales")
                .build();

        RequestDepartmentBean request = new RequestDepartmentBean();
        request.setName("Sales Updated");

        when(departmentsRepository.findByIdAndDeletedFalse(3L)).thenReturn(Optional.of(department));
        when(departmentsRepository.save(any(Departments.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(employeesRepository.countByDepartmentIdAndDeletedFalse(3L)).thenReturn(1L);

        BaseResponse<ResponseDepartmentBean> response = departmentService.updateDepartment(3L, request);

        assertTrue(response.isSuccess());
        assertEquals("Sales Updated", response.getResults().getName());
        assertEquals(1L, response.getResults().getEmployeeCount());
    }

    @Test
    void deleteDepartment_shouldBlockWhenEmployeesExist() {
        Departments department = Departments.builder()
                .id(4L)
                .name("Ops")
                .build();

        when(departmentsRepository.findByIdAndDeletedFalse(4L)).thenReturn(Optional.of(department));
        when(employeesRepository.countByDepartmentIdAndDeletedFalse(4L)).thenReturn(2L);

        assertThrows(ResourceNotFoundException.class, () -> departmentService.deleteDepartment(4L));
    }

    @Test
    void getAllDepartments_shouldReturnMappedDepartments() {
        Departments department = Departments.builder()
                .id(5L)
                .name("Finance")
                .build();

        BaseRequest request = new BaseRequest();
        request.setPage(0);
        request.setSize(10);
        request.setSortBy("id");
        request.setSortDir("asc");

        PageRequest pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
        when(departmentsRepository.findByDeletedFalse(pageable))
                .thenReturn(new PageImpl<>(List.of(department), pageable, 1));
        when(employeesRepository.countEmployeesByDepartment())
                .thenReturn(List.of(countProjection(5L, 7L)));

        BaseResponse<List<ResponseDepartmentBean>> response = departmentService.getAllDepartments(request);

        assertTrue(response.isSuccess());
        assertEquals(1, response.getResults().size());
        assertEquals("Finance", response.getResults().get(0).getName());
        assertEquals(7L, response.getResults().get(0).getEmployeeCount());
        assertEquals(1L, response.getResultInfo().getTotalElements());
    }

    @Test
    void searchDepartments_shouldReturnFilteredDepartments() {
        Departments department = Departments.builder()
                .id(6L)
                .name("DevOps")
                .build();

        SearchDepartmentRequest request = new SearchDepartmentRequest();
        request.setDepartmentName("Dev");
        request.setPage(0);
        request.setSize(10);
        request.setSortBy("id");
        request.setSortDir("asc");

        PageRequest pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
        when(departmentsRepository.findByNameLike("Dev", pageable))
                .thenReturn(new PageImpl<>(List.of(department), pageable, 1));
        when(employeesRepository.countEmployeesByDepartment())
                .thenReturn(List.of());

        BaseResponse<List<ResponseDepartmentBean>> response = departmentService.getDepartmentsByNamePaging(request);

        assertTrue(response.isSuccess());
        assertEquals(1, response.getResults().size());
        assertEquals("DevOps", response.getResults().get(0).getName());
    }

    @Test
    void getDepartmentById_shouldThrowWhenMissing() {
        when(departmentsRepository.findByIdAndDeletedFalse(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> departmentService.getDepartmentById(99L));
    }

    private static com.demo.repository.DepartmentEmployeeCountProjection countProjection(Long departmentId, Long employeeCount) {
        return new com.demo.repository.DepartmentEmployeeCountProjection() {
            @Override
            public Long getDepartmentId() {
                return departmentId;
            }

            @Override
            public Long getEmployeeCount() {
                return employeeCount;
            }
        };
    }
}


