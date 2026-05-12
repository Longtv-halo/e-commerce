package com.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RequestDepartmentBean {

    @NotBlank(message = "Department name is required")
    private String name;
}

