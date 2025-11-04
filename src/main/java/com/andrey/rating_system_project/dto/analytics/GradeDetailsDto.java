package com.andrey.rating_system_project.dto.analytics;

import com.andrey.rating_system_project.model.StudentGrade;
import com.andrey.rating_system_project.model.enums.AssessmentType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class GradeDetailsDto {
    private Long id;
    private AssessmentType assessmentType;
    private Integer mark;
    private BigDecimal preExamAverage;
    private LocalDate examDate;

    public GradeDetailsDto(StudentGrade grade) {
        this.id = grade.getId();
        this.assessmentType = grade.getAssessmentType();
        this.mark = grade.getMark();
        this.preExamAverage = grade.getPreExamAverage();
        this.examDate = grade.getExamDate();
    }
}