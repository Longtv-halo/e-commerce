package com.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseUserBean {

    private Long id;
    private String name;
    private String username;
    private Boolean enabled;

    /** Names of roles assigned to this user */
    private List<String> roles;

    /** All permissions the user has (aggregated from all their roles) */
    private List<String> permissions;
}
