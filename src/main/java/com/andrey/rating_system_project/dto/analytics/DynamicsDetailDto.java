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

    // --- ИЗМЕНЕНИЯ: Вместо штрафа храним часы по типам ---
    private BigDecimal absencePenaltyInSemester; // Оставляем для совместимости (штраф)
    private BigDecimal unexcusedHoursInSemester; // Часы неуважительных пропусков
    private BigDecimal excusedHoursInSemester;   // Часы уважительных пропусков
}