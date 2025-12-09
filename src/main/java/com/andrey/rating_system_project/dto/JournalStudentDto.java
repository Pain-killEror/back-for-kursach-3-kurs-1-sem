package com.andrey.rating_system_project.dto;

import lombok.Data;
import lombok.NoArgsConstructor; // Убедитесь, что эта аннотация есть

import java.util.List;

@Data
@NoArgsConstructor // Lombok создаст пустой конструктор
public class JournalStudentDto {
    private Integer studentId;
    private String studentFullName;
    private List<JournalEventDto> events;
}