package com.andrey.rating_system_project.dto.analytics;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StatItemDto {
    private String label;
    private Long count;

    // Явный конструктор для JPA
    public StatItemDto(String label, Long count) {
        this.label = label;
        this.count = count;
    }
}