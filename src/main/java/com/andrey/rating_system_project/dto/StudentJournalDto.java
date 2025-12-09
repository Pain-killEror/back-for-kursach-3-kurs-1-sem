package com.andrey.rating_system_project.dto;

import lombok.Data;
import java.util.List;

@Data
public class StudentJournalDto {
    private Integer studentId;
    private String fullName;
    // Список строк, например: ["8", "Н", "10", "Н"]
    private List<String> history;
}