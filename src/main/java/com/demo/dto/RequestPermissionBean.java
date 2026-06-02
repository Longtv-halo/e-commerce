package com.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RequestPermissionBean {

    @NotBlank(message = "Permission name is required")
    private String name;

    private String description;
}
