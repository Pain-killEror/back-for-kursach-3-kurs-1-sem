package com.andrey.rating_system_project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// StudentAverageDto.java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentAverageDto {
    private Integer studentId;
    private String studentFullName;
    private String groupName; // <--- ДОБАВИТЬ ЭТО ПОЛЕ
    private BigDecimal averageMark;
    private Long excusedAbsences;
    private Long unexcusedAbsences;
    private BigDecimal extracurricularScore;
}