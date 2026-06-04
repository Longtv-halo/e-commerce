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
public class ResponseRoleBean {

    private Long id;
    private String name;
    private String description;

    /** Names of permissions assigned to this role */
    private List<String> permissions;
}
