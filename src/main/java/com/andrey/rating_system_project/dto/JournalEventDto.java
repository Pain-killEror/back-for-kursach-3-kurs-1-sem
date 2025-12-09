package com.andrey.rating_system_project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class JournalEventDto {
    private LocalDate date;
    private String value; // "8", "Н"
}