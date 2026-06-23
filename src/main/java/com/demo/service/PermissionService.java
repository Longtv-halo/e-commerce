package com.demo.service;

import com.demo.dto.BaseResponse;
import com.demo.dto.RequestPermissionBean;
import com.demo.dto.ResponsePermissionBean;
import com.demo.entity.Permission;
import com.demo.exception.BadRequestException;
import com.demo.exception.ResourceNotFoundException;
import com.demo.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public BaseResponse<List<ResponsePermissionBean>> getAllPermissions() {
        List<ResponsePermissionBean> list = permissionRepository.findAll()
                .stream().map(this::toResponse).toList();
        return BaseResponse.ok(list);
    }

    public BaseResponse<ResponsePermissionBean> getById(Long id) {
        return BaseResponse.ok(toResponse(findById(id)));
    }

    @Transactional
    public BaseResponse<ResponsePermissionBean> createPermission(RequestPermissionBean request) {
        if (permissionRepository.existsByName(request.getName())) {
            throw new BadRequestException("Permission '" + request.getName() + "' already exists");
        }
        Permission perm = permissionRepository.save(Permission.builder()
                .name(request.getName().toUpperCase())
                .description(request.getDescription())
                .build());
        return BaseResponse.created(toResponse(perm));
    }

    @Transactional
    public BaseResponse<ResponsePermissionBean> updatePermission(Long id, RequestPermissionBean request) {
        Permission perm = findById(id);
        perm.setName(request.getName().toUpperCase());
        perm.setDescription(request.getDescription());
        return BaseResponse.ok(toResponse(permissionRepository.save(perm)));
    }

    @Transactional
    public BaseResponse<Void> deletePermission(Long id) {
        permissionRepository.delete(findById(id));
        return BaseResponse.ok(null);
    }

    private Permission findById(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission with id " + id + " not found"));
    }

    public ResponsePermissionBean toResponse(Permission p) {
        return ResponsePermissionBean.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .build();
    }
}
