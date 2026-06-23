package com.demo.controller;

import com.demo.dto.BaseResponse;
import com.demo.dto.RequestRoleBean;
import com.demo.dto.ResponseRoleBean;
import com.demo.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<BaseResponse<List<ResponseRoleBean>>> getAll() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<BaseResponse<ResponseRoleBean>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getById(id));
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<BaseResponse<ResponseRoleBean>> create(@Valid @RequestBody RequestRoleBean request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.createRole(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<BaseResponse<ResponseRoleBean>> update(
            @PathVariable Long id, @Valid @RequestBody RequestRoleBean request) {
        return ResponseEntity.ok(roleService.updateRole(id, request));
    }

    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<BaseResponse<ResponseRoleBean>> assignPermissions(
            @PathVariable Long id, @RequestBody List<Long> permissionIds) {
        return ResponseEntity.ok(roleService.assignPermissions(id, permissionIds));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.deleteRole(id));
    }
}
