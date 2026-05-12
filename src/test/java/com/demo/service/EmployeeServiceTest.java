package com.demo.service;

import com.demo.dto.BaseRequest;
import com.demo.dto.BaseResponse;
import com.demo.dto.RequestEmployeeBean;
import com.demo.dto.ResponseEmployeeBean;
import com.demo.dto.SearchEmployeeRequest;
import com.demo.entity.Departments;
import com.demo.entity.Employees;
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
class EmployeeServiceTest {

    @Mock
    private EmployeesRepository employeesRepository;

    @Mock
    private DepartmentsRepository departmentsRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    void createEmployees_shouldReturnMappedEmployee() {
        Departments department = Departments.builder()
                .id(10L)
                .name("IT")
                .build();

        RequestEmployeeBean request = new RequestEmployeeBean();
        request.setEmpName("Long");
        request.setEmpEmail("long@example.com");
        request.setDepartmentId(10L);

        Employees savedEmployee = Employees.builder()
                .id(1L)
                .name("Long")
                .email("long@example.com")
                .department(department)
                .build();

        when(employeesRepository.existsByEmailIgnoreCaseAndDeletedFalse("long@example.com")).thenReturn(false);
        when(departmentsRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.of(department));
        when(employeesRepository.save(any(Employees.class))).thenReturn(savedEmployee);

        BaseResponse<ResponseEmployeeBean> response = employeeService.createEmployees(request);

        assertTrue(response.isSuccess());
        assertNotNull(response.getResults());
        assertEquals(1L, response.getResults().getId());
        assertEquals("Long", response.getResults().getEmpName());
        assertEquals("long@example.com", response.getResults().getEmpEmail());
        assertEquals(10L, response.getResults().getDepartmentId());
        assertEquals("IT", response.getResults().getDepartmentName());
        verify(employeesRepository).save(any(Employees.class));
    }

    @Test
    void createEmployees_shouldThrowWhenEmailAlreadyExists() {
        RequestEmployeeBean request = new RequestEmployeeBean();
        request.setEmpName("Long");
        request.setEmpEmail("long@example.com");
        request.setDepartmentId(10L);

        when(employeesRepository.existsByEmailIgnoreCaseAndDeletedFalse("long@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> employeeService.createEmployees(request));
    }

    @Test
    void getEmployeeById_shouldReturnEmployeeWithDepartmentInfo() {
        Departments department = Departments.builder()
                .id(3L)
                .name("HR")
                .build();
        Employees employee = Employees.builder()
                .id(7L)
                .name("Mai")
                .email("mai@example.com")
                .department(department)
                .build();

        when(employeesRepository.findByIdAndDeletedFalse(7L)).thenReturn(Optional.of(employee));

        BaseResponse<ResponseEmployeeBean> response = employeeService.getEmployeeById(7L);

        assertTrue(response.isSuccess());
        assertEquals("Mai", response.getResults().getEmpName());
        assertEquals("HR", response.getResults().getDepartmentName());
    }

    @Test
    void getEmployeesByNamePaging_shouldReturnMappedPageData() {
        Departments department = Departments.builder()
                .id(2L)
                .name("Finance")
                .build();
        Employees employee = Employees.builder()
                .id(5L)
                .name("An")
                .email("an@example.com")
                .department(department)
                .build();

        SearchEmployeeRequest request = new SearchEmployeeRequest();
        request.setEmpName("An");
        request.setPage(0);
        request.setSize(10);
        request.setSortBy("id");
        request.setSortDir("asc");

        PageRequest pageable = PageRequest.of(0, 10, Sort.by("id").ascending());

        when(employeesRepository.findByNameLike("An", pageable))
                .thenReturn(new PageImpl<>(List.of(employee), pageable, 1));

        BaseResponse<List<ResponseEmployeeBean>> response = employeeService.getEmployeesByNamePaging(request);

        assertTrue(response.isSuccess());
        assertEquals(1, response.getResults().size());
        assertEquals("Finance", response.getResults().get(0).getDepartmentName());
        assertNotNull(response.getResultInfo());
        assertEquals(1L, response.getResultInfo().getTotalElements());
    }

    @Test
    void deleteEmployees_shouldThrowWhenEmployeeDoesNotExist() {
        when(employeesRepository.findByIdAndDeletedFalse(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.deleteEmployees(99L));
    }

    @Test
    void createEmployees_shouldThrowWhenDepartmentDoesNotExist() {
        RequestEmployeeBean request = new RequestEmployeeBean();
        request.setEmpName("Long");
        request.setEmpEmail("long@example.com");
        request.setDepartmentId(999L);

        when(employeesRepository.existsByEmailIgnoreCaseAndDeletedFalse("long@example.com")).thenReturn(false);
        when(departmentsRepository.findByIdAndDeletedFalse(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.createEmployees(request));
    }

    @Test
    void getAllEmployees_shouldReturnMappedEmployees() {
        Departments department = Departments.builder()
                .id(8L)
                .name("Operations")
                .build();
        Employees employee = Employees.builder()
                .id(11L)
                .name("Lan")
                .email("lan@example.com")
                .department(department)
                .build();

        BaseRequest request = new BaseRequest();
        request.setPage(0);
        request.setSize(5);
        request.setSortBy("id");
        request.setSortDir("asc");

        PageRequest pageable = PageRequest.of(0, 5, Sort.by("id").ascending());

        when(employeesRepository.findByDeletedFalse(pageable))
                .thenReturn(new PageImpl<>(List.of(employee), pageable, 1));

        BaseResponse<List<ResponseEmployeeBean>> response = employeeService.getAllEmployees(request);

        assertTrue(response.isSuccess());
        assertEquals(1, response.getResults().size());
        assertEquals("Operations", response.getResults().get(0).getDepartmentName());
    }
}

