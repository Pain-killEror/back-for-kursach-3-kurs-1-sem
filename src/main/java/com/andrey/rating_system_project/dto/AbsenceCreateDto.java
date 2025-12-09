package com.andrey.rating_system_project.dto;

import com.andrey.rating_system_project.model.enums.AbsenceReasonType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.time.LocalDate;

@Data
public class AbsenceCreateDto {

    @NotNull
    private Integer studentId;

    @NotNull
    private Integer subjectId;

    @NotNull
    private LocalDate absenceDate;

    @NotNull
    @Positive
    private Integer hours;

    @NotNull
    private AbsenceReasonType reasonType; // EXCUSED или UNEXCUSED
}