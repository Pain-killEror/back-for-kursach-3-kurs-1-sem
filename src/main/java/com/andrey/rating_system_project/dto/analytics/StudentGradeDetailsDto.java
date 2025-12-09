package com.andrey.rating_system_project.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentGradeDetailsDto {
    private Integer studentId;
    private String studentFullName;
    private LocalDate date;
    private String mark; // String, чтобы можно было передать "Н" (пропуск)
    private String type; // "grade" или "absence"
}