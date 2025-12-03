package com.andrey.rating_system_project.controller;

import com.andrey.rating_system_project.dto.analytics.AnalyticsRequestDto;
import com.andrey.rating_system_project.dto.analytics.AnalyticsResponseDto;
import com.andrey.rating_system_project.service.AnalyticsService;
import com.andrey.rating_system_project.service.ExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final ExportService exportService;

    public AnalyticsController(AnalyticsService analyticsService, ExportService exportService) {
        this.analyticsService = analyticsService;
        this.exportService = exportService;
    }

    @PostMapping("/query")
    public ResponseEntity<AnalyticsResponseDto> getAnalyticsData(@RequestBody AnalyticsRequestDto request) {
        AnalyticsResponseDto response = analyticsService.getAnalytics(request);
        return ResponseEntity.ok(response);
    }

    // --- НОВЫЙ ЭНДПОИНТ ДЛЯ ЭКСПОРТА ---
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportReport(
            @RequestParam String format,
            @RequestParam Integer studentId,
            @RequestParam(defaultValue = "group") String context,
            @RequestParam(required = false) Long semester,
            // НОВЫЙ ПАРАМЕТР: список колонок (через запятую)
            @RequestParam(defaultValue = "academicScore,extracurricularScore,absencePenalty,totalScore") List<String> columns
    ) throws IOException {

        // Передаем колонки в сервис
        byte[] fileContent = exportService.generateReport(studentId, context, semester, format, columns);

        String filename = "report_student_" + studentId + "." + (format.equals("excel") ? "xlsx" : "pdf");
        MediaType mediaType = format.equals("excel")
                ? MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                : MediaType.APPLICATION_PDF;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(mediaType)
                .body(fileContent);
    }
}