package com.andrey.rating_system_project.controller;

import com.andrey.rating_system_project.dto.*;
import com.andrey.rating_system_project.model.Group;
import com.andrey.rating_system_project.model.StudentAbsence;
import com.andrey.rating_system_project.model.StudentGrade;
import com.andrey.rating_system_project.model.User;
import com.andrey.rating_system_project.repository.*;
import com.andrey.rating_system_project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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

        // Рассчитываем текущий семестр с использованием Java Time API
        Optional<Group> groupOpt = groupRepository.findById(groupId);
        if (groupOpt.isEmpty()) {
            return ResponseEntity.ok(List.of()); // Если группа не найдена
        }
        Group group = groupOpt.get();
        LocalDate now = LocalDate.now();
        int currentSemester = (now.getYear() - group.getFormationYear()) * 2 + (now.getMonthValue() >= 9 ? 1 : 0);

        for (var studentDto : shortDtos) {
            JournalStudentDto journalDto = new JournalStudentDto();
            journalDto.setStudentId(studentDto.getStudentId());
            journalDto.setStudentFullName(studentDto.getStudentFullName());

            List<JournalEventDto> events = new ArrayList<>();

            // Получаем оценки (в будущем здесь будет фильтр по семестру)
            List<StudentGrade> grades = gradeRepository.findByStudentIdAndSubjectId(studentDto.getStudentId(), subjectId);
            grades.forEach(g -> events.add(new JournalEventDto(g.getExamDate(), String.valueOf(g.getMark()))));

            // Получаем пропуски
            List<StudentAbsence> absences = absenceRepository.findByStudentIdAndSubjectId(studentDto.getStudentId(), subjectId);
            absences.forEach(a -> events.add(new JournalEventDto(a.getAbsenceDate(), "Н")));

            journalDto.setEvents(events);
            result.add(journalDto);
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
            BigDecimal avg = gradeRepository.getAverageMarkByStudentAndSubject(student.getStudentId(), subjectId);
            // Округляем до 2 знаков
            avg = avg.setScale(2, BigDecimal.ROUND_HALF_UP);
            result.add(new StudentAverageDto(student.getStudentId(), student.getStudentFullName(), avg));
        }
        return ResponseEntity.ok(result);
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