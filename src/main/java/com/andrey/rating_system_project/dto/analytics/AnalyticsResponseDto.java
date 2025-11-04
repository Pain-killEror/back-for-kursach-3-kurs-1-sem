package com.andrey.rating_system_project.dto.analytics;

import lombok.Data;
import java.util.Map;

@Data
public class AnalyticsResponseDto {
    // Ключ - ID виджета, Значение - данные для него
    private Map<String, WidgetDataDto> widgets;
}