package com.andrey.rating_system_project.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentRankingDto {
    private Integer studentId;
    private String fullName;
    private String groupName;
    private BigDecimal academicScore;
    private BigDecimal extracurricularScore;
    private BigDecimal absencePenalty;
    private BigDecimal unexcusedAbsenceHours; // <-- ИЗМЕНЕНО
    private BigDecimal excusedAbsenceHours;   // <-- НОВОЕ ПОЛЕ
    private BigDecimal totalScore;
}