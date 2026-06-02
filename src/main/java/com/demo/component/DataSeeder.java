package com.demo.component;

import com.demo.entity.Permission;
import com.demo.entity.Role;
import com.demo.entity.Users;
import com.demo.repository.PermissionRepository;
import com.demo.repository.RoleRepository;
import com.demo.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationListener<ContextRefreshedEvent> {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (roleRepository.count() > 0) return;

        Permission empRead   = save("EMPLOYEE_READ",   "View employee list and details");
        Permission empWrite  = save("EMPLOYEE_WRITE",  "Create and update employees");
        Permission empDel    = save("EMPLOYEE_DELETE", "Delete employees");
        Permission deptRead  = save("DEPARTMENT_READ",  "View department list and details");
        Permission deptWrite = save("DEPARTMENT_WRITE", "Create and update departments");
        Permission deptDel   = save("DEPARTMENT_DELETE","Delete departments");
        Permission userRead  = save("USER_READ",   "View users");
        Permission userMgmt  = save("USER_MANAGE", "Manage users: assign roles, lock accounts");
        Permission roleMgmt  = save("ROLE_MANAGE", "Manage roles and permissions");

        Role roleUser = roleRepository.save(Role.builder()
                .name("ROLE_USER")
                .description("Regular user — read-only access")
                .permissions(Set.of(empRead, deptRead))
                .build());

        Role roleManager = roleRepository.save(Role.builder()
                .name("ROLE_MANAGER")
                .description("Department manager — can manage employees and departments")
                .permissions(Set.of(empRead, empWrite, deptRead, deptWrite))
                .build());

        Role roleAdmin = roleRepository.save(Role.builder()
                .name("ROLE_ADMIN")
                .description("System administrator — full access")
                .permissions(Set.of(empRead, empWrite, empDel,
                        deptRead, deptWrite, deptDel,
                        userRead, userMgmt, roleMgmt))
                .build());

        if (!usersRepository.existsByUsername("admin")) {
            usersRepository.save(Users.builder()
                    .name("System Admin")
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .roles(Set.of(roleAdmin))
                    .enabled(true)
                    .build());
        }
    }

    private Permission save(String name, String description) {
        return permissionRepository.save(
                Permission.builder().name(name).description(description).build()
        );
    }
}
