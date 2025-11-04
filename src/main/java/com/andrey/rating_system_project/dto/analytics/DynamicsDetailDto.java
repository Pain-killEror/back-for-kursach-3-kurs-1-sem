package com.andrey.rating_system_project.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DynamicsDetailDto {
    private Long semester;
    private BigDecimal academicScoreInSemester; // Средний балл за этот семестр
    private BigDecimal achievementsInSemester;  // Баллы за достижения в этом семестре
    private BigDecimal absencePenaltyInSemester;  // Штрафы за пропуски в этом семестре
}