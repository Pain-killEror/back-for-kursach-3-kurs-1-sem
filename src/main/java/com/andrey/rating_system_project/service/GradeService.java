package com.andrey.rating_system_project.service;

import com.andrey.rating_system_project.dto.GradeCreateDto;
import com.andrey.rating_system_project.model.StudentGrade;
import com.andrey.rating_system_project.model.Subject;
import com.andrey.rating_system_project.model.User;
import com.andrey.rating_system_project.repository.StudentGradeRepository;
import com.andrey.rating_system_project.repository.SubjectRepository;
import com.andrey.rating_system_project.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class GradeService {

    private final StudentGradeRepository gradeRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;

    public GradeService(StudentGradeRepository gradeRepository, UserRepository userRepository, SubjectRepository subjectRepository) {
        this.gradeRepository = gradeRepository;
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
    }

    @Transactional
    public StudentGrade addGrade(GradeCreateDto gradeDto) {
        // Находим студента, предмет и преподавателя
        User student = userRepository.findById(gradeDto.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Subject subject = subjectRepository.findById(gradeDto.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        // Получаем текущего аутентифицированного пользователя (преподавателя)
        String teacherLogin = SecurityContextHolder.getContext().getAuthentication().getName();
        User teacher = userRepository.findByLogin(teacherLogin)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        // TODO: Добавить проверку, имеет ли этот преподаватель право ставить оценку по этому предмету этому студенту

        StudentGrade grade = new StudentGrade();
        grade.setStudent(student);
        grade.setSubject(subject);
        grade.setTeacher(teacher);
        grade.setAssessmentType(gradeDto.getAssessmentType());
        grade.setMark(gradeDto.getMark());
        grade.setExamDate(gradeDto.getExamDate() != null ? gradeDto.getExamDate() : LocalDate.now());

        // Логику расчета pre_exam_average можно добавить здесь, если это итоговая оценка

        return gradeRepository.save(grade);
    }
}