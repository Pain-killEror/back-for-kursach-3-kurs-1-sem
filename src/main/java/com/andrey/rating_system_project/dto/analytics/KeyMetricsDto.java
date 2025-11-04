package com.andrey.rating_system_project.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeyMetricsDto {
    private BigDecimal overallAverageMark;
    private Long totalStudents;
    private BigDecimal failingStudentsPercentage;
    private BigDecimal excellentStudentsPercentage;
}