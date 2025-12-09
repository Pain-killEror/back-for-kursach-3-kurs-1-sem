package com.andrey.rating_system_project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentAverageDto {
    private Integer studentId;
    private String studentFullName;
    private BigDecimal averageMark; // Средний балл (например, 8.5)
}