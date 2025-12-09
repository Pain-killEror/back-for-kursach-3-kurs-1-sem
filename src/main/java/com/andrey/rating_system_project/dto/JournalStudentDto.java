package com.andrey.rating_system_project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JournalStudentDto {
    private Integer studentId;
    private String studentFullName;
    private String todayMark; // Будет хранить "8", "Н" или null
}