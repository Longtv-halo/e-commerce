package com.demo.controller;

import com.demo.dto.BaseResponse;
import com.demo.dto.RequestPermissionBean;
import com.demo.dto.ResponsePermissionBean;
import com.demo.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permission")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<BaseResponse<List<ResponsePermissionBean>>> getAll() {
        return ResponseEntity.ok(permissionService.getAllPermissions());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<BaseResponse<ResponsePermissionBean>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(permissionService.getById(id));
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<BaseResponse<ResponsePermissionBean>> create(
            @Valid @RequestBody RequestPermissionBean request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(permissionService.createPermission(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<BaseResponse<ResponsePermissionBean>> update(
            @PathVariable Long id, @Valid @RequestBody RequestPermissionBean request) {
        return ResponseEntity.ok(permissionService.updatePermission(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(permissionService.deletePermission(id));
    }
}
