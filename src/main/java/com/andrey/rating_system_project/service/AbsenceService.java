package com.andrey.rating_system_project.service;

import com.andrey.rating_system_project.dto.AbsenceCreateDto;
import com.andrey.rating_system_project.exception.ResourceNotFoundException;
import com.andrey.rating_system_project.model.StudentAbsence;
import com.andrey.rating_system_project.model.Subject;
import com.andrey.rating_system_project.model.User;
import com.andrey.rating_system_project.repository.StudentAbsenceRepository;
import com.andrey.rating_system_project.repository.SubjectRepository;
import com.andrey.rating_system_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AbsenceService {

    private final StudentAbsenceRepository absenceRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;

    @Transactional
    public StudentAbsence addAbsence(AbsenceCreateDto dto) {
        String teacherLogin = SecurityContextHolder.getContext().getAuthentication().getName();
        User teacher = userRepository.findByLogin(teacherLogin)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));

        User student = userRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + dto.getStudentId()));

        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + dto.getSubjectId()));

        // TODO: Добавить проверку, преподает ли этот учитель данный предмет этой группе

        StudentAbsence absence = new StudentAbsence();
        absence.setStudent(student);
        absence.setSubject(subject);
        absence.setTeacher(teacher);
        absence.setAbsenceDate(dto.getAbsenceDate());
        absence.setHours(dto.getHours());
        absence.setReasonType(dto.getReasonType());

        return absenceRepository.save(absence);
    }
}