package com.demo.controller;

import com.demo.dto.BaseRequest;
import com.demo.dto.BaseResponse;
import com.demo.dto.RequestEmployeeBean;
import com.demo.dto.ResponseEmployeeBean;
import com.demo.dto.SearchEmployeeRequest;
import com.demo.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<ResponseEmployeeBean>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @PostMapping("/search")
    public ResponseEntity<BaseResponse<List<ResponseEmployeeBean>>> search(
            @Valid @RequestBody SearchEmployeeRequest request) {
        return ResponseEntity.ok(
                employeeService.getEmployeesByNamePaging(request)
        );
    }

    @PostMapping("/list")
    public ResponseEntity<BaseResponse<List<ResponseEmployeeBean>>> getAll(
            @Valid @RequestBody BaseRequest request) {
        return ResponseEntity.ok(employeeService.getAllEmployees(request));
    }

    @PostMapping("/create")
    public ResponseEntity<BaseResponse<ResponseEmployeeBean>> create(
            @Valid @RequestBody RequestEmployeeBean request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.createEmployees(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<ResponseEmployeeBean>> update(
            @PathVariable Long id,
            @Valid @RequestBody RequestEmployeeBean request) {
        return ResponseEntity.ok(employeeService.updateEmployees(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.deleteEmployees(id));
    }
}
