package com.andrey.rating_system_project.controller;

import com.andrey.rating_system_project.dto.*;
import com.andrey.rating_system_project.model.Group;
import com.andrey.rating_system_project.model.StudentAbsence;
import com.andrey.rating_system_project.model.StudentGrade;
import com.andrey.rating_system_project.model.User;
import com.andrey.rating_system_project.repository.GroupRepository;
import com.andrey.rating_system_project.repository.StudentAbsenceRepository;
import com.andrey.rating_system_project.repository.StudentGradeRepository;
import com.andrey.rating_system_project.repository.UserRepository;
import com.andrey.rating_system_project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal; // Нам нужен только этот импорт из java.math
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
public class TeacherController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final StudentGradeRepository gradeRepository;
    private final StudentAbsenceRepository absenceRepository;
    private final GroupRepository groupRepository;

    @GetMapping("/my-subjects")
    public ResponseEntity<List<SubjectDto>> getMySubjects(Authentication authentication) {
        User teacher = userService.findByLogin(authentication.getName());
        if (teacher.getSubjects() == null) return ResponseEntity.ok(List.of());

        List<SubjectDto> dtos = teacher.getSubjects().stream()
                .map(SubjectDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/group/{groupId}/journal-data")
    public ResponseEntity<List<JournalStudentDto>> getJournalData(
            @PathVariable Integer groupId,
            @RequestParam Integer subjectId
    ) {
        var shortDtos = userRepository.findStudentsByGroupId(groupId);

        List<JournalStudentDto> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (var student : shortDtos) {
            String markValue = null;

            Optional<StudentGrade> grade = gradeRepository.findFirstByStudentIdAndSubjectIdAndExamDate(
                    student.getStudentId(), subjectId, today);

            if (grade.isPresent()) {
                markValue = String.valueOf(grade.get().getMark());
            } else {
                Optional<StudentAbsence> absence = absenceRepository.findFirstByStudentIdAndSubjectIdAndAbsenceDate(
                        student.getStudentId(), subjectId, today);

                if (absence.isPresent()) {
                    markValue = "Н";
                }
            }

            result.add(new JournalStudentDto(student.getStudentId(), student.getStudentFullName(), markValue));
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/group/{groupId}/performance")
    public ResponseEntity<List<StudentAverageDto>> getGroupPerformance(
            @PathVariable Integer groupId,
            @RequestParam Integer subjectId
    ) {
        var shortDtos = userRepository.findStudentsByGroupId(groupId);
        List<StudentAverageDto> result = new ArrayList<>();

        for (var student : shortDtos) {
            // Теперь этот метод ВСЕГДА вернет BigDecimal (например, 8.00 или 0.00)
            BigDecimal avg = gradeRepository.getAverageMarkByStudentAndSubject(student.getStudentId(), subjectId);

            // Округляем до 2 знаков (COALESCE уже вернул 0.00, если оценок не было)
            avg = avg.setScale(2, BigDecimal.ROUND_HALF_UP);

            result.add(new StudentAverageDto(student.getStudentId(), student.getStudentFullName(), avg));
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/my-groups")
    public ResponseEntity<List<GroupDto>> getMyGroups(
            @RequestParam Integer subjectId,
            Authentication authentication) {

        User teacher = userService.findByLogin(authentication.getName());

        // Это сложный запрос, его лучше вынести в сервис и репозиторий.
        // Он должен найти все группы, у студентов которых есть предмет `subjectId`.
        // Пока сделаем упрощенную версию:
        // Вернем все группы, где специальность студентов изучает этот предмет.

        List<Group> allGroups = groupRepository.findAll(); // Предполагаем, что у вас есть groupRepository

        List<GroupDto> relevantGroups = allGroups.stream()
                .filter(group -> {
                    // Проверяем, есть ли в curriculum для специальности этой группы наш предмет
                    // В реальном проекте это делается ОДНИМ SQL-запросом!
                    if (group.getSpecialty() == null) return false;

                    // Тут нужен доступ к CurriculumRepository
                    // boolean hasSubject = curriculumRepository.existsBySpecialtyIdAndSubjectId(...)
                    // Для примера просто вернем все группы, т.к. нет доступа ко всем репо.
                    return true;
                })
                .map(GroupDto::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(relevantGroups);
    }

    @GetMapping("/my-relevant-groups")
    public ResponseEntity<List<GroupShortDto>> getMyRelevantGroups(
            @RequestParam Integer subjectId,
            Authentication authentication) {

        User teacher = userService.findByLogin(authentication.getName());

        List<Object[]> results = groupRepository.findRelevantGroupsForTeacher(teacher.getId(), subjectId);

        List<GroupShortDto> dtos = results.stream()
                .map(row -> new GroupShortDto((Integer) row[0], (String) row[1]))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}