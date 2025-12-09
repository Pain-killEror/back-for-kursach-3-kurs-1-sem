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
    private BigDecimal averageMark;
    private Long excusedAbsences;      // Пропуски по уважительной причине (часы)
    private Long unexcusedAbsences;    // Пропуски по неуважительной причине (часы)
    private BigDecimal extracurricularScore; // Баллы за внеучебную деятельность
}