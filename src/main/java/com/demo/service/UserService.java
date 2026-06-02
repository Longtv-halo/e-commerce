package com.demo.service;

import com.demo.dto.BaseResponse;
import com.demo.dto.RequestAssignRolesBean;
import com.demo.dto.ResponseUserBean;
import com.demo.entity.Permission;
import com.demo.entity.Role;
import com.demo.entity.Users;
import com.demo.exception.BadRequestException;
import com.demo.exception.ResourceNotFoundException;
import com.demo.repository.RoleRepository;
import com.demo.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UsersRepository usersRepository;
    private final RoleRepository roleRepository;

    public BaseResponse<List<ResponseUserBean>> getAllUsers(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Users> userPage = usersRepository.findAll(pageable);
        return BaseResponse.ok(userPage.getContent().stream().map(this::toResponse).toList());
    }

    public BaseResponse<ResponseUserBean> getUserById(Long id) {
        return BaseResponse.ok(toResponse(findById(id)));
    }

    public BaseResponse<ResponseUserBean> getMyProfile(String username) {
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return BaseResponse.ok(toResponse(user));
    }

    @Transactional
    public BaseResponse<ResponseUserBean> assignRoles(Long userId, RequestAssignRolesBean request) {
        Users user = findById(userId);
        List<Role> roles = roleRepository.findAllById(request.getRoleIds());
        if (roles.size() != request.getRoleIds().size()) throw new BadRequestException("One or more role IDs not found");
        user.setRoles(new HashSet<>(roles));
        return BaseResponse.ok(toResponse(usersRepository.save(user)));
    }

    @Transactional
    public BaseResponse<ResponseUserBean> toggleStatus(Long userId) {
        Users user = findById(userId);
        user.setEnabled(!user.getEnabled());
        return BaseResponse.ok(toResponse(usersRepository.save(user)));
    }

    @Transactional
    public BaseResponse<Void> deleteUser(Long userId) {
        usersRepository.delete(findById(userId));
        return BaseResponse.ok(null);
    }

    private Users findById(Long id) {
        return usersRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
    }

    private ResponseUserBean toResponse(Users user) {
        return ResponseUserBean.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .enabled(user.getEnabled())
                .roles(user.getRoles().stream().map(Role::getName).sorted().toList())
                .permissions(user.getRoles().stream()
                        .flatMap(r -> r.getPermissions().stream())
                        .map(Permission::getName)
                        .distinct().sorted().toList())
                .build();
    }
}
