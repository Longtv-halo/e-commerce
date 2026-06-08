package com.demo.controller;

import com.demo.dto.*;
import com.demo.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/department")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DEPARTMENT_READ') and @abacEval.isAllowed(authentication, 'DEPARTMENT', #id)")
    public ResponseEntity<BaseResponse<ResponseDepartmentBean>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }

    @PostMapping("/search")
    @PreAuthorize("hasAuthority('DEPARTMENT_READ') and @abacEval.isAllowed(authentication, 'DEPARTMENT', null)")
    public ResponseEntity<BaseResponse<List<ResponseDepartmentBean>>> search(
            @Valid @RequestBody SearchDepartmentRequest request) {
        return ResponseEntity.ok(departmentService.getDepartmentsByNamePaging(request));
    }

    @PostMapping("/list")
    @PreAuthorize("hasAuthority('DEPARTMENT_READ') and @abacEval.isAllowed(authentication, 'DEPARTMENT', null)")
    public ResponseEntity<BaseResponse<List<ResponseDepartmentBean>>> getAll(
            @Valid @RequestBody BaseRequest request) {
        return ResponseEntity.ok(departmentService.getAllDepartments(request));
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('DEPARTMENT_WRITE') and @abacEval.isAllowed(authentication, 'DEPARTMENT', null)")
    public ResponseEntity<BaseResponse<ResponseDepartmentBean>> create(
            @Valid @RequestBody RequestDepartmentBean request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(departmentService.createDepartment(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DEPARTMENT_WRITE') and @abacEval.isAllowed(authentication, 'DEPARTMENT', #id)")
    public ResponseEntity<BaseResponse<ResponseDepartmentBean>> update(
            @PathVariable Long id,
            @Valid @RequestBody RequestDepartmentBean request) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DEPARTMENT_DELETE') and @abacEval.isAllowed(authentication, 'DEPARTMENT', #id)")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.deleteDepartment(id));
    }

    @PutMapping("/assign{id}")
    @PreAuthorize("hasAuthority('DEPARTMENT_WRITE') and @abacEval.isAllowed(authentication, 'DEPARTMENT', #id)")
    public ResponseEntity<BaseResponse<ResponseDepartmentBean>> assign(
            @PathVariable Long id,
            @Valid @RequestBody RequestAssignRoleDepartmentBean request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(departmentService.changeRoleLeader(id, request));
    }
}
