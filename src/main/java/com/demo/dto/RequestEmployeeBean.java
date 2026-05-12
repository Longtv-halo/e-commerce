package com.demo.dto;

import com.demo.validator.DepartmentExists;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RequestEmployeeBean {

    @NotBlank(message = "Employee name is required")
    private String empName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Employee email is required")
    private String empEmail;

    @NotNull(message = "Department id is required")
    @DepartmentExists(message = "Department not found")
    private Long departmentId;
}
