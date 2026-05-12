package com.demo.controller;

import com.demo.dto.BaseRequest;
import com.demo.dto.BaseResponse;
import com.demo.dto.RequestDepartmentBean;
import com.demo.dto.ResponseDepartmentBean;
import com.demo.dto.SearchDepartmentRequest;
import com.demo.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/department")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<ResponseDepartmentBean>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }

    @PostMapping("/search")
    public ResponseEntity<BaseResponse<List<ResponseDepartmentBean>>> search(
            @Valid @RequestBody SearchDepartmentRequest request) {
        return ResponseEntity.ok(departmentService.getDepartmentsByNamePaging(request));
    }

    @PostMapping("/list")
    public ResponseEntity<BaseResponse<List<ResponseDepartmentBean>>> getAll(
            @Valid @RequestBody BaseRequest request) {
        return ResponseEntity.ok(departmentService.getAllDepartments(request));
    }

    @PostMapping("/create")
    public ResponseEntity<BaseResponse<ResponseDepartmentBean>> create(
            @Valid @RequestBody RequestDepartmentBean request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.createDepartment(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<ResponseDepartmentBean>> update(
            @PathVariable Long id,
            @Valid @RequestBody RequestDepartmentBean request) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.deleteDepartment(id));
    }
}

