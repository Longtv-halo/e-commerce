package com.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class AuthResponse {

    private String token;

    @Builder.Default
    private String type = "Bearer";

    private String username;

    /** All roles assigned to the user (e.g. ["ROLE_ADMIN", "ROLE_MANAGER"]) */
    private List<String> roles;
}
