package com.andrey.rating_system_project.controller;

import com.andrey.rating_system_project.dto.analytics.AnalyticsRequestDto;
import com.andrey.rating_system_project.dto.analytics.AnalyticsResponseDto;
import com.andrey.rating_system_project.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
//@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @PostMapping("/query")
    public ResponseEntity<AnalyticsResponseDto> getAnalyticsData(@RequestBody AnalyticsRequestDto request) {
        AnalyticsResponseDto response = analyticsService.getAnalytics(request);
        return ResponseEntity.ok(response);
    }
}