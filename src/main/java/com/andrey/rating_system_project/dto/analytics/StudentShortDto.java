package com.andrey.rating_system_project.dto.analytics;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StudentShortDto {
    private Integer studentId;
    private String studentFullName;

    // Явный конструктор для JPA
    public StudentShortDto(Integer studentId, String studentFullName) {
        this.studentId = studentId;
        this.studentFullName = studentFullName;
    }
}