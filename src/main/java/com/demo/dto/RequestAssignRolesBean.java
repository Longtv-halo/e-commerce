package com.demo.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class RequestAssignRolesBean {

    /** List of role IDs to assign to the user (replaces existing roles) */
    @NotEmpty(message = "At least one role ID is required")
    private List<Long> roleIds;
}
