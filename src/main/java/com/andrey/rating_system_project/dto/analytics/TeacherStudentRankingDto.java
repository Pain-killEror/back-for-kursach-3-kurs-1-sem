package com.andrey.rating_system_project.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherStudentRankingDto {
    private Integer studentId;
    private String studentFullName;
    private String groupName;
    private BigDecimal averageMark;
    private BigDecimal extracurricularScore;
    private Long excusedAbsences;
    private Long unexcusedAbsences;
    private BigDecimal totalScore;
}