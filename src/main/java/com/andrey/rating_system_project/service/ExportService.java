package com.andrey.rating_system_project.service;

import com.andrey.rating_system_project.dto.analytics.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExportService {

    private final AnalyticsService analyticsService;

    // Словарь названий колонок
    private static final Map<String, String> COLUMN_TITLES = Map.of(
            "academicScore", "Академ.",
            "extracurricularScore", "Внеуч.",
            "absencePenalty", "Штраф",
            "totalScore", "Итого"
    );

    public ExportService(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    public byte[] generateReport(Integer studentId, String context, Long semester, String format, List<String> columns) throws IOException {
        // 1. Получение данных
        Map<String, Object> filters = new HashMap<>();
        filters.put("studentId", studentId);
        filters.put("comparisonContext", context);
        if (semester != null) {
            filters.put("rankingSemester", semester);
            filters.put("semester", semester);
        }
        filters.put("lines", List.of("cumulativeTotal"));

        AnalyticsRequestDto request = new AnalyticsRequestDto();
        request.setFilters(filters);
        request.setWidgetIds(List.of("myScores", "myRank", "myScoreBreakdown", "studentRankingList"));

        AnalyticsResponseDto data = analyticsService.getAnalytics(request);

        MyScoresDto scores = (MyScoresDto) data.getWidgets().get("myScores").getData();
        StudentRankDto rank = (StudentRankDto) data.getWidgets().get("myRank").getData();

        WidgetDataDto breakdownWidget = data.getWidgets().get("myScoreBreakdown");
        Map<String, Object> breakdownMap = (Map<String, Object>) breakdownWidget.getData();
        List<ContributionItemDto> breakdownList = (List<ContributionItemDto>) breakdownMap.get("breakdown");

        WidgetDataDto rankingWidget = data.getWidgets().get("studentRankingList");
        Map<String, Object> rankingMap = (Map<String, Object>) rankingWidget.getData();
        List<StudentRankingDto> fullRankingList = (List<StudentRankingDto>) rankingMap.get("data");

        List<StudentRankingDto> filteredRankingList = filterRankingListSmart(fullRankingList, studentId, context);

        if ("pdf".equalsIgnoreCase(format)) {
            return createPdf(scores, rank, breakdownList, filteredRankingList, fullRankingList, studentId, context, semester, columns);
        } else {
            return createExcel(scores, rank, breakdownList, filteredRankingList, fullRankingList, studentId, context, semester, columns);
        }
    }

    private List<StudentRankingDto> filterRankingListSmart(List<StudentRankingDto> fullList, Integer myId, String context) {
        if ("group".equals(context)) return fullList;
        int myIndex = -1;
        for (int i = 0; i < fullList.size(); i++) {
            if (fullList.get(i).getStudentId().equals(myId)) {
                myIndex = i;
                break;
            }
        }
        if (myIndex == -1) return fullList;
        int start = Math.max(0, myIndex - 10);
        int end = Math.min(fullList.size(), myIndex + 11);
        return fullList.subList(start, end);
    }

    // ================= PDF GENERATION =================

    private byte[] createPdf(MyScoresDto scores, StudentRankDto rank, List<ContributionItemDto> breakdown,
                             List<StudentRankingDto> rankingList, List<StudentRankingDto> fullList,
                             Integer myId, String context, Long semester, List<String> columns) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, out);
            document.open();

            BaseFont bf;
            try {
                ClassPathResource fontResource = new ClassPathResource("fonts/arial.ttf");
                if (fontResource.exists()) {
                    bf = BaseFont.createFont(fontResource.getPath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                } else {
                    bf = BaseFont.createFont("c:/windows/fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                }
            } catch (Exception e) {
                bf = BaseFont.createFont(BaseFont.HELVETICA, "Cp1251", BaseFont.NOT_EMBEDDED);
            }

            com.lowagie.text.Font titleFont = new com.lowagie.text.Font(bf, 16, com.lowagie.text.Font.BOLD);
            com.lowagie.text.Font headerFont = new com.lowagie.text.Font(bf, 10, com.lowagie.text.Font.BOLD);
            com.lowagie.text.Font normalFont = new com.lowagie.text.Font(bf, 10, com.lowagie.text.Font.NORMAL);
            com.lowagie.text.Font boldFont = new com.lowagie.text.Font(bf, 10, com.lowagie.text.Font.BOLD);
            com.lowagie.text.Font smallFont = new com.lowagie.text.Font(bf, 9, com.lowagie.text.Font.ITALIC, Color.GRAY);

            Paragraph title = new Paragraph("Отчет об успеваемости", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            String semText = semester != null ? "Семестр " + semester : "Накопительный итог";
            Paragraph subTitle = new Paragraph("Студент №" + myId + " | " + semText, headerFont);
            subTitle.setAlignment(Element.ALIGN_CENTER);
            subTitle.setSpacingAfter(5);
            document.add(subTitle);

            String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            Paragraph dateP = new Paragraph("Сформировано: " + dateStr, smallFont);
            dateP.setAlignment(Element.ALIGN_RIGHT);
            dateP.setSpacingAfter(15);
            document.add(dateP);

            // Динамическое создание таблицы рейтинга
            // 3 обязательные колонки (Место, ID, Группа) + количество выбранных
            int totalCols = 3 + columns.size();
            float[] widths = new float[totalCols];
            widths[0] = 1f; // Место
            widths[1] = 2f; // ID
            widths[2] = 2f; // Группа
            for (int i = 3; i < totalCols; i++) widths[i] = 2f; // Остальные

            PdfPTable rankingTable = new PdfPTable(widths);
            rankingTable.setWidthPercentage(100);
            rankingTable.setSpacingBefore(5);

            // Заголовки
            addHeaderCell(rankingTable, "Место", headerFont);
            addHeaderCell(rankingTable, "ID", headerFont);
            addHeaderCell(rankingTable, "Группа", headerFont);
            for (String col : columns) {
                addHeaderCell(rankingTable, COLUMN_TITLES.getOrDefault(col, col), headerFont);
            }

            // Данные
            for (StudentRankingDto dto : rankingList) {
                int realRank = fullList.indexOf(dto) + 1;
                boolean isMe = dto.getStudentId().equals(myId);
                Color bgColor = isMe ? new Color(230, 247, 255) : null;
                com.lowagie.text.Font rowFont = isMe ? boldFont : normalFont;

                addCell(rankingTable, String.valueOf(realRank), normalFont, bgColor);
                addCell(rankingTable, String.valueOf(dto.getStudentId()), normalFont, bgColor);
                addCell(rankingTable, dto.getGroupName() != null ? dto.getGroupName() : "-", normalFont, bgColor);

                // Динамические данные
                for (String col : columns) {
                    String val = getDtoValue(dto, col);
                    addCell(rankingTable, val, rowFont, bgColor);
                }
            }
            document.add(rankingTable);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF Error", e);
        }
    }

    // ================= EXCEL GENERATION =================

    private byte[] createExcel(MyScoresDto scores, StudentRankDto rank, List<ContributionItemDto> breakdown,
                               List<StudentRankingDto> rankingList, List<StudentRankingDto> fullList,
                               Integer myId, String context, Long semester, List<String> columns) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Отчет");

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle normalStyle = workbook.createCellStyle();
            normalStyle.setAlignment(HorizontalAlignment.CENTER);
            normalStyle.setBorderBottom(BorderStyle.THIN);
            normalStyle.setBorderLeft(BorderStyle.THIN);
            normalStyle.setBorderRight(BorderStyle.THIN);

            CellStyle highlightStyle = workbook.createCellStyle();
            highlightStyle.cloneStyleFrom(normalStyle);
            highlightStyle.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
            highlightStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            int rowNum = 0;
            Row titleRow = sheet.createRow(rowNum++);
            titleRow.createCell(0).setCellValue("Отчет студента №" + myId);

            String semText = semester != null ? "Семестр " + semester : "Накопительный итог";
            sheet.createRow(rowNum++).createCell(0).setCellValue("Период: " + semText);
            rowNum++;

            // Динамические заголовки
            sheet.createRow(rowNum++).createCell(0).setCellValue("Рейтинг:");
            Row rankHead = sheet.createRow(rowNum++);

            int colIdx = 0;
            createHeaderCell(rankHead, colIdx++, "Место", headerStyle);
            createHeaderCell(rankHead, colIdx++, "ID", headerStyle);
            createHeaderCell(rankHead, colIdx++, "Группа", headerStyle);
            for (String col : columns) {
                createHeaderCell(rankHead, colIdx++, COLUMN_TITLES.getOrDefault(col, col), headerStyle);
            }

            for (StudentRankingDto dto : rankingList) {
                Row r = sheet.createRow(rowNum++);
                int realRank = fullList.indexOf(dto) + 1;
                boolean isMe = dto.getStudentId().equals(myId);
                CellStyle style = isMe ? highlightStyle : normalStyle;

                colIdx = 0;
                createStyledCell(r, colIdx++, realRank, style);
                createStyledCell(r, colIdx++, dto.getStudentId(), style);
                createStyledCell(r, colIdx++, dto.getGroupName(), style);

                for (String col : columns) {
                    // Здесь получаем числовое значение для Excel, чтобы работали формулы
                    Number val = getDtoValueNumber(dto, col);
                    createStyledCell(r, colIdx++, val, style);
                }
            }

            for(int i=0; i<colIdx; i++) sheet.autoSizeColumn(i);

            workbook.write(out);
            return out.toByteArray();
        }
    }

    // === Вспомогательные методы ===

    private void addHeaderCell(PdfPTable table, String text, com.lowagie.text.Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new Color(240, 240, 240));
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addCell(PdfPTable table, String text, com.lowagie.text.Font font, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        if (bg != null) cell.setBackgroundColor(bg);
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void createHeaderCell(Row row, int idx, String text, CellStyle style) {
        Cell c = row.createCell(idx);
        c.setCellValue(text);
        c.setCellStyle(style);
    }

    private void createStyledCell(Row row, int idx, Object val, CellStyle style) {
        Cell c = row.createCell(idx);
        if (val instanceof Number) c.setCellValue(((Number) val).doubleValue());
        else c.setCellValue(val.toString());
        c.setCellStyle(style);
    }

    // Метод для извлечения данных из DTO по имени поля (String для PDF)
    private String getDtoValue(StudentRankingDto dto, String col) {
        BigDecimal val = getDtoValueNumber(dto, col);
        return String.format("%.2f", val);
    }

    // Метод для извлечения данных из DTO по имени поля (BigDecimal для Excel)
    private BigDecimal getDtoValueNumber(StudentRankingDto dto, String col) {
        switch (col) {
            case "academicScore": return dto.getAcademicScore();
            case "extracurricularScore": return dto.getExtracurricularScore();
            case "absencePenalty": return dto.getAbsencePenalty();
            case "totalScore": return dto.getTotalScore();
            default: return BigDecimal.ZERO;
        }
    }

    private String translateCategory(String cat) {
        switch (cat) {
            case "ACADEMIC": return "Учеба";
            case "SCIENCE": return "Наука";
            case "SOCIAL": return "Общественная";
            case "SPORTS": return "Спорт";
            case "CULTURE": return "Культура";
            default: return cat;
        }
    }
}