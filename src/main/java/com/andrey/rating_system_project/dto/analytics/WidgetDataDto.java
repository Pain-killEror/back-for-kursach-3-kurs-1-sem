package com.andrey.rating_system_project.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WidgetDataDto {
    private String title;
    private String suggestedChartType; // Рекомендуемый тип диаграммы
    private Object data; // Сами данные (могут быть списком, числом, объектом)
}