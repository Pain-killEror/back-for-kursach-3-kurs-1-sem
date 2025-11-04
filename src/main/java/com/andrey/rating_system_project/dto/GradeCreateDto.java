package com.andrey.rating_system_project.dto;

import com.andrey.rating_system_project.model.enums.AssessmentType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class GradeCreateDto {
    @NotNull
    private Integer studentId;

    @NotNull
    private Integer subjectId;

    @NotNull
    private AssessmentType assessmentType;

    @NotNull
    @Min(0)
    @Max(10)
    private Integer mark;

    private LocalDate examDate;
}