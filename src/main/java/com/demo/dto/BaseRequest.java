package com.demo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class BaseRequest {
    @Min(value = 0, message = "page must be >= 0")
    private Integer page = 0;

    @Min(value = 1, message = "size must be >= 1")
    @Max(value = 100, message = "size must be <= 100")
    private Integer size = 10;

    private String sortBy = "id";

    private String sortDir = "asc";
}
