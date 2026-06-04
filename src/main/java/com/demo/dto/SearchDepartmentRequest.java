package com.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SearchDepartmentRequest extends BaseRequest {

    @NotBlank(message = "Department name is required")
    private String departmentName;
}

