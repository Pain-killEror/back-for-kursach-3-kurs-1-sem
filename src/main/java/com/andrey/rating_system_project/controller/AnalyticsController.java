package com.andrey.rating_system_project.controller;

import com.andrey.rating_system_project.dto.analytics.AnalyticsRequestDto;
import com.andrey.rating_system_project.dto.analytics.AnalyticsResponseDto;
import com.andrey.rating_system_project.dto.analytics.StatItemDto;
import com.andrey.rating_system_project.service.AnalyticsService;
import com.andrey.rating_system_project.service.ExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @GetMapping("/admin-report")
    public ResponseEntity<byte[]> exportAdminReport() throws IOException {
        // 1. Получаем данные для виджетов, как если бы мы запрашивали их для дашборда
        // Для этого создаем временный запрос
        AnalyticsRequestDto requestDto = new AnalyticsRequestDto();
        Map<String, Object> filters = new HashMap<>();
        filters.put("adminId", 0); // Добавляем ключ, чтобы сработала логика для администратора. Значение не важно.
        requestDto.setFilters(filters);
        requestDto.setWidgetIds(List.of("roleStatistics", "userStatusOverview"));

        AnalyticsResponseDto analyticsData = analyticsService.getAnalytics(requestDto);

        // 2. Извлекаем данные из ответа
        List<StatItemDto> roleStats = (List<StatItemDto>) analyticsData.getWidgets().get("roleStatistics").getData();
        List<StatItemDto> userStatusStats = (List<StatItemDto>) analyticsData.getWidgets().get("userStatusOverview").getData();

        // 3. Генерируем PDF
        byte[] pdfContent = exportService.generateAdminReport(roleStats, userStatusStats);

        String filename = "admin_report_" + LocalDate.now() + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfContent);
    }

    @GetMapping("/dean-report")
    public ResponseEntity<byte[]> downloadDeanReport(
            @RequestParam Integer facultyId,
            @RequestParam Integer formationYear
    ) throws IOException {
        byte[] pdfContent = exportService.generateDeanReport(facultyId, formationYear);

        String filename = "dean_report.pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfContent);
    }

    // В классе AnalyticsController добавьте этот метод:

    @GetMapping("/global-report")
    @PreAuthorize("hasAnyAuthority('RECTORATE_STAFF', 'ADMINISTRATOR')")
    public ResponseEntity<byte[]> downloadGlobalReport() throws IOException {
        // Сервис сам соберет все данные
        byte[] pdfContent = exportService.generateGlobalReport();

        String filename = "Global_University_Report_" + LocalDate.now() + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfContent);
    }
}