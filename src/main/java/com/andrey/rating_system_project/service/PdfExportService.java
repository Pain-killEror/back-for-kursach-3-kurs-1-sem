package com.andrey.rating_system_project.service;

import com.andrey.rating_system_project.dto.StudentAverageDto;
import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfExportService {

    private final TeacherService teacherService;

    // Путь к нашему шрифту
    private static final String FONT_PATH = "fonts/arial.ttf";

    public byte[] generatePerformanceReport(Integer subjectId, List<Integer> groupIds) throws IOException, DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate());

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            // --- Инициализация шрифтов ---
            BaseFont baseFont = BaseFont.createFont(new ClassPathResource(FONT_PATH).getPath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font titleFont = new Font(baseFont, 18, Font.BOLD);
            Font groupFont = new Font(baseFont, 14, Font.BOLD);
            Font headerFont = new Font(baseFont, 10, Font.BOLD, Color.WHITE);
            Font cellFont = new Font(baseFont, 9);

            // --- Заголовок документа ---
            Paragraph title = new Paragraph("Отчет по успеваемости", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);
            boolean isFirstGroup = true;
            // --- Таблицы для каждой группы ---
            for (Integer groupId : groupIds) {

                if (!isFirstGroup) {
                    document.newPage();
                }

                List<StudentAverageDto> students = teacherService.getGroupPerformance(groupId, subjectId);
                if (students.isEmpty()) continue;

                // Название группы
                String groupName = students.get(0).getGroupName();
                Paragraph groupTitle = new Paragraph("Группа: " + groupName, groupFont);
                document.add(groupTitle);

                // Создание таблицы
                PdfPTable table = new PdfPTable(5);
                table.setWidthPercentage(100);
                table.setSpacingBefore(10f);
                table.setWidths(new float[]{3f, 1f, 1f, 1f, 1.5f});

                // Заголовки таблицы
                addHeaderCell(table, "Студент", headerFont);
                addHeaderCell(table, "Ср. балл", headerFont);
                addHeaderCell(table, "Проп. (Ув.)", headerFont);
                addHeaderCell(table, "Проп. (Неув.)", headerFont);
                addHeaderCell(table, "Внеучеб. баллы", headerFont);

                // Заполнение данными
                for (StudentAverageDto student : students) {
                    addCell(table, student.getStudentFullName(), cellFont, Element.ALIGN_LEFT);
                    addCell(table, String.valueOf(student.getAverageMark()), cellFont, Element.ALIGN_CENTER);
                    addCell(table, student.getExcusedAbsences() + " ч.", cellFont, Element.ALIGN_CENTER);
                    addCell(table, student.getUnexcusedAbsences() + " ч.", cellFont, Element.ALIGN_CENTER);
                    addCell(table, String.valueOf(student.getExtracurricularScore()), cellFont, Element.ALIGN_CENTER);
                }

                document.add(table);
                document.add(Chunk.NEWLINE); // Отступ между таблицами
                isFirstGroup = false;
            }

        } finally {
            document.close();
        }
        return baos.toByteArray();
    }

    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell header = new PdfPCell(new Phrase(text, font));
        header.setBackgroundColor(new Color(0, 123, 255)); // Синий фон
        header.setHorizontalAlignment(Element.ALIGN_CENTER);
        header.setVerticalAlignment(Element.ALIGN_MIDDLE);
        header.setPadding(5);
        table.addCell(header);
    }

    private void addCell(PdfPTable table, String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        table.addCell(cell);
    }
}