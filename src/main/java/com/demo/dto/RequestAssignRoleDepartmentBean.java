package com.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RequestAssignRoleDepartmentBean {

    @NotBlank(message = "Department name is required")
    private String name;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Employee email is required")
    private String ownerEmail;

    private String newOwnerName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Employee email is required")
    private String newOwnerEmail;
}

