package com.andrey.rating_system_project.dto.analytics;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class AnalyticsRequestDto {
    private Map<String, Object> filters;
    private List<String> widgetIds;
}