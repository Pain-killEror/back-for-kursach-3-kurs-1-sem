package com.andrey.rating_system_project.service;

import com.andrey.rating_system_project.dto.StudentAverageDto;
import com.andrey.rating_system_project.dto.StudentJournalDto;
import com.andrey.rating_system_project.model.StudentAbsence;
import com.andrey.rating_system_project.model.StudentGrade;
import com.andrey.rating_system_project.model.User;
import com.andrey.rating_system_project.model.enums.AbsenceReasonType;
import com.andrey.rating_system_project.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
    private final AchievementRepository achievementRepository;
    private final GroupRepository groupRepository;

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

    public List<StudentAverageDto> getGroupPerformance(Integer groupId, Integer subjectId) {
        // Получаем группу, чтобы знать ее имя
        String groupName = groupRepository.findById(groupId)
                .map(g -> g.getName())
                .orElse("Неизвестная группа");

        var students = userRepository.findStudentsByGroupId(groupId);
        List<StudentAverageDto> result = new ArrayList<>();

        for (var student : students) {
            BigDecimal avgMark = gradeRepository.getAverageMarkByStudentAndSubject(student.getStudentId(), subjectId)
                    .setScale(2, BigDecimal.ROUND_HALF_UP);

            Long excused = absenceRepository.countTotalHoursByStudentAndReason(student.getStudentId(), AbsenceReasonType.EXCUSED);
            Long unexcused = absenceRepository.countTotalHoursByStudentAndReason(student.getStudentId(), AbsenceReasonType.UNEXCUSED);
            BigDecimal extraScore = achievementRepository.sumPointsByStudentId(student.getStudentId());

            result.add(new StudentAverageDto(
                    student.getStudentId(),
                    student.getStudentFullName(),
                    groupName, // <--- Передаем имя группы
                    avgMark,
                    excused,
                    unexcused,
                    extraScore
            ));
        }

        return result;
    }
}