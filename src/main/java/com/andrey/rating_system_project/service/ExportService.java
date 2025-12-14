package com.andrey.rating_system_project.service;

import com.andrey.rating_system_project.dto.analytics.*;
import com.andrey.rating_system_project.exception.ResourceNotFoundException;
import com.andrey.rating_system_project.model.Faculty;
import com.andrey.rating_system_project.repository.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExportService {

    private final AnalyticsService analyticsService;
    private final StudentGradeRepository studentGradeRepository;
    private final AchievementRepository achievementRepository;
    private final FacultyRepository facultyRepository;
    private final StudentInfoRepository studentInfoRepository;
    private final UserRepository userRepository;

    // Шрифты
    private BaseFont bf;
    private com.lowagie.text.Font titleFont;
    private com.lowagie.text.Font headerFont;
    private com.lowagie.text.Font normalFont;
    private com.lowagie.text.Font boldFont;
    private com.lowagie.text.Font smallFont;
    private com.lowagie.text.Font groupFont;
    private com.lowagie.text.Font facultyFont;

    // Словарь названий колонок
    private static final Map<String, String> COLUMN_TITLES = Map.of(
            "academicScore", "Академ.",
            "extracurricularScore", "Внеуч.",
            "absencePenalty", "Штраф",
            "totalScore", "Итого"
    );

    public ExportService(AnalyticsService analyticsService,
                         StudentGradeRepository studentGradeRepository,
                         AchievementRepository achievementRepository,
                         FacultyRepository facultyRepository,
                         StudentInfoRepository studentInfoRepository,
                         UserRepository userRepository) {
        this.analyticsService = analyticsService;
        this.studentGradeRepository = studentGradeRepository;
        this.achievementRepository = achievementRepository;
        this.facultyRepository = facultyRepository;
        this.studentInfoRepository = studentInfoRepository;
        this.userRepository = userRepository;
        initFonts();
    }

    private void initFonts() {
        try {
            ClassPathResource fontResource = new ClassPathResource("fonts/arial.ttf");
            if (fontResource.exists()) {
                bf = BaseFont.createFont(fontResource.getPath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            } else {
                bf = BaseFont.createFont("c:/windows/fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            }
        } catch (Exception e) {
            try {
                bf = BaseFont.createFont(BaseFont.HELVETICA, "Cp1251", BaseFont.NOT_EMBEDDED);
            } catch (Exception ignored) {}
        }
        titleFont = new com.lowagie.text.Font(bf, 18, com.lowagie.text.Font.BOLD);
        headerFont = new com.lowagie.text.Font(bf, 10, com.lowagie.text.Font.BOLD);
        normalFont = new com.lowagie.text.Font(bf, 10, com.lowagie.text.Font.NORMAL);
        boldFont = new com.lowagie.text.Font(bf, 10, com.lowagie.text.Font.BOLD);
        smallFont = new com.lowagie.text.Font(bf, 9, com.lowagie.text.Font.ITALIC, Color.GRAY);
        groupFont = new com.lowagie.text.Font(bf, 14, com.lowagie.text.Font.BOLD, new Color(0, 86, 179));
        facultyFont = new com.lowagie.text.Font(bf, 16, com.lowagie.text.Font.BOLD);
    }

    // ================= GLOBAL REPORT (RECTOR) =================

    public byte[] generateGlobalReport() throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, out);
            document.open();

            // 1. Титульный лист
            Paragraph mainTitle = new Paragraph("ГЛОБАЛЬНЫЙ ОТЧЕТ УНИВЕРСИТЕТА", titleFont);
            mainTitle.setAlignment(Element.ALIGN_CENTER);
            mainTitle.setSpacingAfter(10);
            document.add(mainTitle);

            Paragraph dateP = new Paragraph("Сформировано: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")), smallFont);
            dateP.setAlignment(Element.ALIGN_CENTER);
            dateP.setSpacingAfter(30);
            document.add(dateP);

            // 2. Секция РЕКТОРА (Сводка по ВУЗу)
            writeRectorReportContent(document);

            document.newPage();

            // 3. Секция АДМИНИСТРАТОРА (Системная статистика)
            List<StatItemDto> roleStats = userRepository.countUsersByRole();
            List<StatItemDto> userStatusStats = userRepository.countUsersByStatus();
            writeAdminReportContent(document, roleStats, userStatusStats);

            // 4. Секция ДЕКАНОВ (Детальный рейтинг)
            List<Faculty> faculties = facultyRepository.findAll();
            int currentYear = LocalDate.now().getYear();
            int currentMonth = LocalDate.now().getMonthValue();
            int academicYearStart = (currentMonth >= 9) ? currentYear : currentYear - 1;

            for (Faculty faculty : faculties) {
                for (int course = 1; course <= 4; course++) {
                    int formationYear = academicYearStart - (course - 1);
                    document.newPage();

                    // Разделитель разделов
                    PdfPTable headerTable = new PdfPTable(1);
                    headerTable.setWidthPercentage(100);
                    PdfPCell cell = new PdfPCell(new Phrase("Факультет: " + faculty.getName() + " | Курс: " + course, facultyFont));
                    cell.setBackgroundColor(new Color(230, 230, 230));
                    cell.setPadding(10);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setBorder(0);
                    headerTable.addCell(cell);
                    document.add(headerTable);
                    document.add(Chunk.NEWLINE);

                    writeDeanReportContent(document, faculty.getId(), faculty.getName(), formationYear);
                }
            }

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generating Global Report", e);
        }
    }

    private void writeRectorReportContent(Document document) throws DocumentException {
        Paragraph title = new Paragraph("1. Сводные показатели по ВУЗу", new com.lowagie.text.Font(bf, 14, com.lowagie.text.Font.BOLD));
        title.setSpacingAfter(15);
        document.add(title);

        // Таблица 1: Сравнение успеваемости факультетов
        List<Object[]> perfData = studentGradeRepository.getFacultyPerformanceComparison();
        if (!perfData.isEmpty()) {
            document.add(new Paragraph("Средняя успеваемость по факультетам:", boldFont));
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(60);
            table.setHorizontalAlignment(Element.ALIGN_LEFT);
            table.setSpacingBefore(5);
            table.setSpacingAfter(15);
            addHeaderCell(table, "Факультет", headerFont);
            addHeaderCell(table, "Средний балл", headerFont);

            for (Object[] row : perfData) {
                String fName = (String) row[0];
                Double avg = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
                addCell(table, fName, normalFont, null);
                addCell(table, String.format("%.2f", avg), normalFont, null);
            }
            document.add(table);
        }

        // Таблица 2: Формы обучения
        List<StatItemDto> eduData = studentInfoRepository.countByEducationForm();
        if (!eduData.isEmpty()) {
            document.add(new Paragraph("Распределение по формам обучения:", boldFont));
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(40);
            table.setHorizontalAlignment(Element.ALIGN_LEFT);
            table.setSpacingBefore(5);
            table.setSpacingAfter(15);
            addHeaderCell(table, "Форма", headerFont);
            addHeaderCell(table, "Кол-во студентов", headerFont);

            long total = 0;
            for (StatItemDto item : eduData) {
                String label = "BUDGET".equals(item.getLabel()) ? "Бюджет" : "Платно";
                addCell(table, label, normalFont, null);
                addCell(table, String.valueOf(item.getCount()), normalFont, null);
                total += item.getCount();
            }
            addCell(table, "ВСЕГО", boldFont, new Color(240,240,240));
            addCell(table, String.valueOf(total), boldFont, new Color(240,240,240));
            document.add(table);
        }

        // Таблица 3: Внеучебная деятельность
        List<FacultyActivityDto> activityData = achievementRepository.getFacultyExtracurricularActivity();
        if (!activityData.isEmpty()) {
            document.add(new Paragraph("Внеучебная активность по направлениям:", boldFont));
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(80);
            table.setHorizontalAlignment(Element.ALIGN_LEFT);
            table.setSpacingBefore(5);
            addHeaderCell(table, "Факультет", headerFont);
            addHeaderCell(table, "Категория", headerFont);
            addHeaderCell(table, "Баллы", headerFont);

            for (FacultyActivityDto item : activityData) {
                addCell(table, item.getFacultyName(), normalFont, null);
                addCell(table, translateCategory(item.getCategory()), normalFont, null);
                addCell(table, String.format("%.0f", item.getTotalPoints()), normalFont, null);
            }
            document.add(table);
        }
    }

    // ================= ADMIN REPORT =================

    public byte[] generateAdminReport(List<StatItemDto> roleStats, List<StatItemDto> userStatusStats) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            Paragraph title = new Paragraph("Административный отчет", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            Paragraph dateP = new Paragraph("Сформировано: " + dateStr, smallFont);
            dateP.setAlignment(Element.ALIGN_RIGHT);
            dateP.setSpacingAfter(20);
            document.add(dateP);

            writeAdminReportContent(document, roleStats, userStatusStats);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating Admin PDF", e);
        }
    }

    private void writeAdminReportContent(Document document, List<StatItemDto> roleStats, List<StatItemDto> userStatusStats) throws DocumentException {
        // Таблица Ролей
        Paragraph sectionTitle = new Paragraph("Распределение персонала по ролям", new com.lowagie.text.Font(bf, 12, com.lowagie.text.Font.BOLD));
        sectionTitle.setSpacingBefore(15);
        sectionTitle.setSpacingAfter(10);
        document.add(sectionTitle);

        PdfPTable rolesTable = new PdfPTable(2);
        rolesTable.setWidthPercentage(100);
        addHeaderCell(rolesTable, "Роль", headerFont);
        addHeaderCell(rolesTable, "Количество", headerFont);
        for (StatItemDto item : roleStats) {
            rolesTable.addCell(new Phrase(translateLabel(item.getLabel(), true), normalFont));
            rolesTable.addCell(new Phrase(String.valueOf(item.getCount()), normalFont));
        }
        document.add(rolesTable);

        // Таблица Статусов
        sectionTitle = new Paragraph("Статистика по статусам пользователей", new com.lowagie.text.Font(bf, 12, com.lowagie.text.Font.BOLD));
        sectionTitle.setSpacingBefore(15);
        sectionTitle.setSpacingAfter(10);
        document.add(sectionTitle);

        PdfPTable statusTable = new PdfPTable(2);
        statusTable.setWidthPercentage(100);
        addHeaderCell(statusTable, "Статус", headerFont);
        addHeaderCell(statusTable, "Количество", headerFont);
        for (StatItemDto item : userStatusStats) {
            statusTable.addCell(new Phrase(translateLabel(item.getLabel(), false), normalFont));
            statusTable.addCell(new Phrase(String.valueOf(item.getCount()), normalFont));
        }
        document.add(statusTable);
    }

    // ================= DEAN REPORT =================

    public byte[] generateDeanReport(Integer facultyId, Integer formationYear) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            Faculty faculty = facultyRepository.findById(facultyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Faculty not found"));

            Paragraph facultyTitle = new Paragraph(faculty.getName(), new com.lowagie.text.Font(bf, 14, com.lowagie.text.Font.BOLD));
            facultyTitle.setAlignment(Element.ALIGN_CENTER);
            facultyTitle.setSpacingAfter(5);
            document.add(facultyTitle);

            writeDeanReportContent(document, facultyId, faculty.getName(), formationYear);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating Dean PDF", e);
        }
    }

    private void writeDeanReportContent(Document document, Integer facultyId, String facultyName, Integer formationYear) throws DocumentException {
        Paragraph title = new Paragraph("Отчет по успеваемости (Год набора: " + formationYear + ")", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        List<StudentRankingDto> allStudents = studentGradeRepository.getStudentRankingList(
                facultyId, formationYear, null, null, null
        );

        Map<String, List<StudentRankingDto>> studentsByGroup = allStudents.stream()
                .collect(Collectors.groupingBy(s -> s.getGroupName() != null ? s.getGroupName() : "Без группы"));

        if (studentsByGroup.isEmpty()) {
            document.add(new Paragraph("Нет данных о студентах за этот период.", normalFont));
        }

        for (Map.Entry<String, List<StudentRankingDto>> entry : studentsByGroup.entrySet()) {
            String groupName = entry.getKey();
            List<StudentRankingDto> students = entry.getValue();

            Paragraph groupTitle = new Paragraph("Группа " + groupName, groupFont);
            groupTitle.setSpacingBefore(15);
            groupTitle.setSpacingAfter(5);
            document.add(groupTitle);

            PdfPTable table = new PdfPTable(new float[]{1, 4, 2, 2, 2, 2});
            table.setWidthPercentage(100);

            addHeaderCell(table, "№", headerFont);
            addHeaderCell(table, "ФИО", headerFont);
            addHeaderCell(table, "Академ.", headerFont);
            addHeaderCell(table, "Внеуч.", headerFont);
            addHeaderCell(table, "Штраф", headerFont);
            addHeaderCell(table, "Итого", headerFont);

            int i = 1;
            for (StudentRankingDto s : students) {
                addCell(table, String.valueOf(i++), normalFont, null);
                addCell(table, s.getFullName(), normalFont, null);
                addCell(table, String.format("%.2f", s.getAcademicScore()), normalFont, null);
                addCell(table, String.format("%.2f", s.getExtracurricularScore()), normalFont, null);
                addCell(table, String.format("%.2f", s.getAbsencePenalty()), normalFont, null);
                addCell(table, String.format("%.2f", s.getTotalScore()), boldFont, new Color(230, 247, 255));
            }
            document.add(table);
        }

        // Вклад в рейтинг
        List<ContributionItemDto> contributions = achievementRepository.getAchievementsContribution(facultyId, formationYear, null);
        if (!contributions.isEmpty()) {
            Paragraph contribTitle = new Paragraph("Статистика: Вклад в рейтинг", new com.lowagie.text.Font(bf, 14, com.lowagie.text.Font.BOLD));
            contribTitle.setSpacingBefore(20);
            contribTitle.setSpacingAfter(10);
            document.add(contribTitle);

            PdfPTable contribTable = new PdfPTable(new float[]{3, 2});
            contribTable.setWidthPercentage(60);
            contribTable.setHorizontalAlignment(Element.ALIGN_LEFT);

            addHeaderCell(contribTable, "Категория", headerFont);
            addHeaderCell(contribTable, "Сумма баллов", headerFont);

            for (ContributionItemDto item : contributions) {
                String categoryName = translateCategory(item.getCategory());
                addCell(contribTable, categoryName, normalFont, null);
                addCell(contribTable, String.format("%.2f", item.getTotalPoints()), normalFont, null);
            }
            document.add(contribTable);
        }
    }

    // ================= STUDENT REPORT =================

    public byte[] generateReport(Integer studentId, String context, Long semester, String format, List<String> columns) throws IOException {
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

    private byte[] createPdf(MyScoresDto scores, StudentRankDto rank, List<ContributionItemDto> breakdown,
                             List<StudentRankingDto> rankingList, List<StudentRankingDto> fullList,
                             Integer myId, String context, Long semester, List<String> columns) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, out);
            document.open();

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

            int totalCols = 3 + columns.size();
            float[] widths = new float[totalCols];
            widths[0] = 1f; widths[1] = 2f; widths[2] = 2f;
            for (int i = 3; i < totalCols; i++) widths[i] = 2f;

            PdfPTable rankingTable = new PdfPTable(widths);
            rankingTable.setWidthPercentage(100);
            rankingTable.setSpacingBefore(5);

            addHeaderCell(rankingTable, "Место", headerFont);
            addHeaderCell(rankingTable, "ID", headerFont);
            addHeaderCell(rankingTable, "Группа", headerFont);
            for (String col : columns) {
                addHeaderCell(rankingTable, COLUMN_TITLES.getOrDefault(col, col), headerFont);
            }

            for (StudentRankingDto dto : rankingList) {
                int realRank = fullList.indexOf(dto) + 1;
                boolean isMe = dto.getStudentId().equals(myId);
                Color bgColor = isMe ? new Color(230, 247, 255) : null;
                com.lowagie.text.Font rowFont = isMe ? boldFont : normalFont;

                addCell(rankingTable, String.valueOf(realRank), normalFont, bgColor);
                addCell(rankingTable, String.valueOf(dto.getStudentId()), normalFont, bgColor);
                addCell(rankingTable, dto.getGroupName() != null ? dto.getGroupName() : "-", normalFont, bgColor);

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

    private byte[] createExcel(MyScoresDto scores, StudentRankDto rank, List<ContributionItemDto> breakdown,
                               List<StudentRankingDto> rankingList, List<StudentRankingDto> fullList,
                               Integer myId, String context, Long semester, List<String> columns) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Отчет");

            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font font = workbook.createFont();
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
                    Number val = getDtoValueNumber(dto, col);
                    createStyledCell(r, colIdx++, val, style);
                }
            }

            for(int i=0; i<colIdx; i++) sheet.autoSizeColumn(i);

            workbook.write(out);
            return out.toByteArray();
        }
    }

    // === Helpers ===

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

    private String getDtoValue(StudentRankingDto dto, String col) {
        BigDecimal val = getDtoValueNumber(dto, col);
        return String.format("%.2f", val);
    }

    private BigDecimal getDtoValueNumber(StudentRankingDto dto, String col) {
        switch (col) {
            case "academicScore": return dto.getAcademicScore();
            case "extracurricularScore": return dto.getExtracurricularScore();
            case "absencePenalty": return dto.getAbsencePenalty();
            case "totalScore": return dto.getTotalScore();
            default: return BigDecimal.ZERO;
        }
    }

    private String translateLabel(String label, boolean isRole) {
        if (isRole) {
            switch(label) {
                case "ADMINISTRATOR": return "Администратор";
                case "DEAN_STAFF": return "Сотрудник деканата";
                case "TEACHER": return "Преподаватель";
                case "STUDENT": return "Студент";
                case "RECTORATE_STAFF": return "Сотрудник ректората";
                default: return label;
            }
        } else {
            switch(label) {
                case "ACTIVE": return "Активные";
                case "PENDING": return "Ожидают подтверждения";
                case "BLOCKED": return "Заблокированные";
                default: return label;
            }
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