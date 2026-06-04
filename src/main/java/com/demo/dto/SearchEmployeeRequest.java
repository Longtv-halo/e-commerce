package com.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SearchEmployeeRequest extends BaseRequest {

    @NotBlank(message = "Employee name is required")
    private String empName;
}

