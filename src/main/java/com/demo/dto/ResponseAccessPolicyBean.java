package com.demo.dto;

import com.demo.entity.abac.PolicyDayOfWeek;
import com.demo.entity.abac.PolicyEffect;
import com.demo.entity.abac.PolicySubjectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseAccessPolicyBean {

    private Long id;

    private String resourceType;

    private Long resourceId;

    private PolicySubjectType subjectType;

    private Long subjectId;

    private PolicyDayOfWeek dayOfWeek;

    private LocalTime startTime;

    private LocalTime endTime;

    private PolicyEffect effect;

    private Boolean enabled;

    private String description;
}
