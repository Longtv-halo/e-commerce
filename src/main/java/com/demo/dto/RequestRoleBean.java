package com.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class RequestRoleBean {

    @NotBlank(message = "Role name is required")
    private String name;

    private String description;

    /** List of permission IDs to assign to this role */
    private List<Long> permissionIds;
}
