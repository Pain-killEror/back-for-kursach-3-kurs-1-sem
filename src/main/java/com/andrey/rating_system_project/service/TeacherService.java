package com.andrey.rating_system_project.service;

import com.andrey.rating_system_project.dto.StudentJournalDto;
import com.andrey.rating_system_project.model.StudentAbsence;
import com.andrey.rating_system_project.model.StudentGrade;
import com.andrey.rating_system_project.model.User;
import com.andrey.rating_system_project.repository.StudentAbsenceRepository;
import com.andrey.rating_system_project.repository.StudentGradeRepository;
import com.andrey.rating_system_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final UserRepository userRepository;
    private final StudentGradeRepository gradeRepository;
    private final StudentAbsenceRepository absenceRepository;

    // Вспомогательный класс для сортировки
    private record JournalEntry(LocalDate date, String value) {}

    public List<StudentJournalDto> getGroupJournal(Integer groupId, Integer subjectId, Integer teacherId) {
        // 1. Получаем список студентов группы
        List<User> students = userRepository.findAll().stream() // Лучше использовать findByGroupId в репозитории
                .filter(u -> u.getStudentInfo() != null &&
                        u.getStudentInfo().getGroup() != null &&
                        u.getStudentInfo().getGroup().getId().equals(groupId))
                .toList();

        List<StudentJournalDto> journal = new ArrayList<>();

        for (User student : students) {
            StudentJournalDto dto = new StudentJournalDto();
            dto.setStudentId(student.getId());
            dto.setFullName(student.getFullName());

            // 2. Получаем оценки
            List<JournalEntry> entries = new ArrayList<>();

            // Ищем оценки только по этому предмету у этого преподавателя (или вообще по предмету)
            // Для упрощения берем по предмету и преподавателю
            List<StudentGrade> grades = gradeRepository.findAll().stream()
                    .filter(g -> g.getStudent().getId().equals(student.getId()) &&
                            g.getSubject().getId().equals(subjectId)) // && g.getTeacher().getId().equals(teacherId) - опционально
                    .toList();

            for (StudentGrade g : grades) {
                entries.add(new JournalEntry(g.getExamDate(), String.valueOf(g.getMark())));
            }

            // 3. Получаем пропуски
            List<StudentAbsence> absences = absenceRepository.findAll().stream()
                    .filter(a -> a.getStudent().getId().equals(student.getId()) &&
                            a.getSubject().getId().equals(subjectId))
                    .toList();

            for (StudentAbsence a : absences) {
                entries.add(new JournalEntry(a.getAbsenceDate(), "Н"));
            }

            // 4. Сортируем по дате
            dto.setHistory(entries.stream()
                    .sorted(Comparator.comparing(JournalEntry::date))
                    .map(JournalEntry::value)
                    .collect(Collectors.toList()));

            journal.add(dto);
        }

        // Сортировка студентов по фамилии
        journal.sort(Comparator.comparing(StudentJournalDto::getFullName));
        return journal;
    }
}