package com.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseEmployeeBean {
	private Long id;
	private String empName;
	private String empEmail;
	private Long departmentId;
	private String departmentName;
}