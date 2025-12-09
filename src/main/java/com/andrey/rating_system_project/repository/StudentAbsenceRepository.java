package com.andrey.rating_system_project.repository;

import com.andrey.rating_system_project.model.StudentAbsence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentAbsenceRepository extends JpaRepository<StudentAbsence, Long> {
    // Эффективный поиск пропуска
    Optional<StudentAbsence> findFirstByStudentIdAndSubjectIdAndAbsenceDate(Integer studentId, Integer subjectId, LocalDate absenceDate);

    List<StudentAbsence> findByStudentIdAndSubjectId(Integer studentId, Integer subjectId);
}