package com.andrey.rating_system_project.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistributionItemDto {
    private String label; // Например, "9-10"
    private Long count;   // Количество студентов
}