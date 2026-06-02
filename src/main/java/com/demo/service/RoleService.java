package com.demo.service;

import com.demo.dto.BaseResponse;
import com.demo.dto.RequestRoleBean;
import com.demo.dto.ResponseRoleBean;
import com.demo.entity.Permission;
import com.demo.entity.Role;
import com.demo.exception.BadRequestException;
import com.demo.exception.ResourceNotFoundException;
import com.demo.repository.PermissionRepository;
import com.demo.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public BaseResponse<List<ResponseRoleBean>> getAllRoles() {
        return BaseResponse.ok(roleRepository.findAll().stream().map(this::toResponse).toList());
    }

    public BaseResponse<ResponseRoleBean> getById(Long id) {
        return BaseResponse.ok(toResponse(findById(id)));
    }

    @Transactional
    public BaseResponse<ResponseRoleBean> createRole(RequestRoleBean request) {
        if (roleRepository.existsByName(request.getName())) {
            throw new BadRequestException("Role '" + request.getName() + "' already exists");
        }
        Role role = roleRepository.save(Role.builder()
                .name(request.getName().toUpperCase())
                .description(request.getDescription())
                .permissions(resolvePermissions(request.getPermissionIds()))
                .build());
        return BaseResponse.created(toResponse(role));
    }

    @Transactional
    public BaseResponse<ResponseRoleBean> updateRole(Long id, RequestRoleBean request) {
        Role role = findById(id);
        role.setName(request.getName().toUpperCase());
        role.setDescription(request.getDescription());
        if (request.getPermissionIds() != null) {
            role.setPermissions(resolvePermissions(request.getPermissionIds()));
        }
        return BaseResponse.ok(toResponse(roleRepository.save(role)));
    }

    @Transactional
    public BaseResponse<ResponseRoleBean> assignPermissions(Long roleId, List<Long> permissionIds) {
        Role role = findById(roleId);
        role.setPermissions(resolvePermissions(permissionIds));
        return BaseResponse.ok(toResponse(roleRepository.save(role)));
    }

    @Transactional
    public BaseResponse<Void> deleteRole(Long id) {
        roleRepository.delete(findById(id));
        return BaseResponse.ok(null);
    }

    private Role findById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role with id " + id + " not found"));
    }

    private Set<Permission> resolvePermissions(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return new HashSet<>();
        List<Permission> found = permissionRepository.findAllById(ids);
        if (found.size() != ids.size()) throw new BadRequestException("One or more permission IDs not found");
        return new HashSet<>(found);
    }

    public ResponseRoleBean toResponse(Role role) {
        return ResponseRoleBean.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .permissions(role.getPermissions().stream().map(Permission::getName).sorted().toList())
                .build();
    }
}
