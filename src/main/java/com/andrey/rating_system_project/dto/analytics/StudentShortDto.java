package com.andrey.rating_system_project.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentShortDto {
    private Integer studentId;
    private String studentFullName;
}