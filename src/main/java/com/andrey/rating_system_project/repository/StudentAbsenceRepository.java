package com.andrey.rating_system_project.repository;

import com.andrey.rating_system_project.model.StudentAbsence;
import com.andrey.rating_system_project.model.enums.AbsenceReasonType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentAbsenceRepository extends JpaRepository<StudentAbsence, Long> {
    // Эффективный поиск пропуска
    Optional<StudentAbsence> findFirstByStudentIdAndSubjectIdAndAbsenceDate(Integer studentId, Integer subjectId, LocalDate absenceDate);

    List<StudentAbsence> findByStudentIdAndSubjectId(Integer studentId, Integer subjectId);

    @Query("SELECT COALESCE(SUM(sa.hours), 0) FROM StudentAbsence sa " +
            "WHERE sa.student.id = :studentId AND sa.reasonType = :reasonType")
    Long countTotalHoursByStudentAndReason(
            @Param("studentId") Integer studentId,
            @Param("reasonType") AbsenceReasonType reasonType);
}