package com.andrey.rating_system_project.repository;

import com.andrey.rating_system_project.dto.analytics.*;
import com.andrey.rating_system_project.model.StudentGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentGradeRepository extends JpaRepository<StudentGrade, Long> {

    @Query(value = """
            WITH StudentAverage AS (
                SELECT sg.student_user_id, AVG(sg.mark) as avg_mark
                FROM student_grades sg
                JOIN users u ON sg.student_user_id = u.id
                JOIN students_info si ON u.id = si.user_id
                JOIN `groups` g ON si.group_id = g.id
                WHERE (:assessmentTypes IS NULL OR sg.assessment_type IN (:assessmentTypes))
                AND (:facultyId IS NULL OR g.faculty_id = :facultyId)
                AND (:formationYear IS NULL OR g.formation_year = :formationYear)
                AND (:groupId IS NULL OR g.id = :groupId)
                AND (:educationForm IS NULL OR si.education_form = :educationForm)
                GROUP BY sg.student_user_id
            )
            SELECT
                CASE
                    WHEN avg_mark >= 9 THEN '9-10' WHEN avg_mark >= 8 THEN '8-9'
                    WHEN avg_mark >= 7 THEN '7-8' WHEN avg_mark >= 6 THEN '6-7'
                    WHEN avg_mark >= 5 THEN '5-6' WHEN avg_mark >= 4 THEN '4-5' ELSE '0-4'
                END AS label, COUNT(*) as count
            FROM StudentAverage GROUP BY label ORDER BY label DESC
            """, nativeQuery = true)
    List<DistributionItemDto> getPerformanceDistribution(@Param("facultyId") Integer facultyId, @Param("formationYear") Integer formationYear, @Param("groupId") Integer groupId, @Param("assessmentTypes") List<String> assessmentTypes, @Param("educationForm") String educationForm);

    @Query(value = """
            SELECT GREATEST(1, (YEAR(sg.exam_date) - g.formation_year - IF(MONTH(sg.exam_date) < 9, 1, 0)) * 2 + IF(MONTH(sg.exam_date) < 9, 2, 1)) AS semester, AVG(sg.mark) as averageMark
            FROM student_grades sg
            JOIN users u ON sg.student_user_id = u.id
            JOIN students_info si ON u.id = si.user_id
            JOIN `groups` g ON si.group_id = g.id
            WHERE (:assessmentTypes IS NULL OR sg.assessment_type IN (:assessmentTypes))
            AND (:facultyId IS NULL OR g.faculty_id = :facultyId)
            AND (:formationYear IS NULL OR g.formation_year = :formationYear)
            AND (:groupId IS NULL OR g.id = :groupId)
            AND (:educationForm IS NULL OR si.education_form = :educationForm)
            GROUP BY semester ORDER BY semester ASC
            """, nativeQuery = true)
    List<DynamicsPointDto> getAverageScoreDynamics(@Param("facultyId") Integer facultyId, @Param("formationYear") Integer formationYear, @Param("groupId") Integer groupId, @Param("assessmentTypes") List<String> assessmentTypes, @Param("educationForm") String educationForm);

    @Query(value = """
            WITH StudentAverage AS (
                SELECT sg.student_user_id, AVG(sg.mark) as avg_mark
                FROM student_grades sg
                JOIN users u ON sg.student_user_id = u.id
                JOIN students_info si ON u.id = si.user_id
                JOIN `groups` g ON si.group_id = g.id
                WHERE (:assessmentTypes IS NULL OR sg.assessment_type IN (:assessmentTypes))
                AND (:facultyId IS NULL OR g.faculty_id = :facultyId)
                AND (:formationYear IS NULL OR g.formation_year = :formationYear)
                AND (:groupId IS NULL OR g.id = :groupId)
                AND (:educationForm IS NULL OR si.education_form = :educationForm)
                GROUP BY sg.student_user_id
            )
            SELECT AVG(avg_mark) AS overallAverageMark, COUNT(*) AS totalStudents,
                (SUM(CASE WHEN avg_mark < 4 THEN 1 ELSE 0 END) * 100.0 / COUNT(*)) AS failingStudentsPercentage,
                (SUM(CASE WHEN avg_mark >= 9 THEN 1 ELSE 0 END) * 100.0 / COUNT(*)) AS excellentStudentsPercentage
            FROM StudentAverage
            """, nativeQuery = true)
    KeyMetricsDto getKeyMetrics(@Param("facultyId") Integer facultyId, @Param("formationYear") Integer formationYear, @Param("groupId") Integer groupId, @Param("assessmentTypes") List<String> assessmentTypes, @Param("educationForm") String educationForm);

    @Query(value = """
            SELECT g.name AS groupName, AVG(sg.mark) AS averageMark
            FROM student_grades sg
            JOIN users u ON sg.student_user_id = u.id
            JOIN students_info si ON u.id = si.user_id
            JOIN `groups` g ON si.group_id = g.id
            WHERE (:assessmentTypes IS NULL OR sg.assessment_type IN (:assessmentTypes))
            AND (:facultyId IS NULL OR g.faculty_id = :facultyId)
            AND (:formationYear IS NULL OR g.formation_year = :formationYear)
            AND (:educationForm IS NULL OR si.education_form = :educationForm)
            GROUP BY g.id, g.name ORDER BY averageMark DESC
            """, nativeQuery = true)
    List<ComparisonItemDto> getGroupComparison(@Param("facultyId") Integer facultyId, @Param("formationYear") Integer formationYear, @Param("assessmentTypes") List<String> assessmentTypes, @Param("educationForm") String educationForm);

    @Query(value = """
            SELECT SUM(sg.mark)
            FROM student_grades sg
            JOIN users u ON sg.student_user_id = u.id
            JOIN students_info si ON u.id = si.user_id
            JOIN `groups` g ON si.group_id = g.id
            WHERE (:assessmentTypes IS NULL OR sg.assessment_type IN (:assessmentTypes))
            AND (:facultyId IS NULL OR g.faculty_id = :facultyId)
            AND (:formationYear IS NULL OR g.formation_year = :formationYear)
            AND (:groupId IS NULL OR g.id = :groupId)
            AND (:educationForm IS NULL OR si.education_form = :educationForm)
            """, nativeQuery = true)
    BigDecimal getTotalAcademicPoints(@Param("facultyId") Integer facultyId, @Param("formationYear") Integer formationYear, @Param("groupId") Integer groupId, @Param("assessmentTypes") List<String> assessmentTypes, @Param("educationForm") String educationForm);

    // --- CUMULATIVE RANKING ---
    @Query(value = """
            WITH AcademicScores AS (
                SELECT sg.student_user_id AS student_id, AVG(sg.mark) AS academic_score
                FROM student_grades sg
                WHERE (:assessmentTypes IS NULL OR sg.assessment_type IN (:assessmentTypes))
                GROUP BY sg.student_user_id
            ),
            ExtracurricularScores AS (
                SELECT a.student_user_id AS student_id, SUM(a.points_awarded) AS extracurricular_score
                FROM achievements a
                GROUP BY a.student_user_id
            ),
            AbsenceData AS (
                SELECT sa.student_user_id AS student_id,
                    SUM(CASE WHEN sa.reason_type = 'UNEXCUSED' THEN sa.hours ELSE 0 END) AS unexcused_hours,
                    SUM(CASE WHEN sa.reason_type = 'EXCUSED' THEN sa.hours ELSE 0 END) AS excused_hours,
                    SUM(CASE WHEN sa.reason_type = 'UNEXCUSED' THEN sa.hours ELSE 0 END) * 0.1 AS absence_penalty
                FROM student_absences sa
                GROUP BY sa.student_user_id
            )
            SELECT u.id AS studentId, u.full_name AS fullName, g.name AS groupName,
                COALESCE(acs.academic_score, 0) AS academicScore,
                COALESCE(ecs.extracurricular_score, 0) AS extracurricularScore,
                COALESCE(ad.absence_penalty, 0) AS absencePenalty,
                COALESCE(ad.unexcused_hours, 0) AS unexcusedAbsenceHours,
                COALESCE(ad.excused_hours, 0) AS excusedAbsenceHours,
                (COALESCE(acs.academic_score, 0) + COALESCE(ecs.extracurricular_score, 0) - COALESCE(ad.absence_penalty, 0)) AS totalScore
            FROM users u
            JOIN students_info si ON u.id = si.user_id
            JOIN `groups` g ON si.group_id = g.id
            LEFT JOIN AcademicScores acs ON u.id = acs.student_id
            LEFT JOIN ExtracurricularScores ecs ON u.id = ecs.student_id
            LEFT JOIN AbsenceData ad ON u.id = ad.student_id
            WHERE u.role_id = (SELECT id FROM roles WHERE name = 'STUDENT')
                AND (:facultyId IS NULL OR g.faculty_id = :facultyId)
                AND (:formationYear IS NULL OR g.formation_year = :formationYear)
                AND (:groupId IS NULL OR g.id = :groupId)
                AND (:educationForm IS NULL OR si.education_form = :educationForm)
            ORDER BY totalScore DESC
            """, nativeQuery = true)
    List<StudentRankingDto> getStudentRankingList(@Param("facultyId") Integer facultyId, @Param("formationYear") Integer formationYear, @Param("groupId") Integer groupId, @Param("assessmentTypes") List<String> assessmentTypes, @Param("educationForm") String educationForm);

    // --- SEMESTER RANKING (FIXED) ---
    @Query(value = """
            WITH SemesterData AS (
                -- 1. Grades (aux_value = 0)
                SELECT sg.id, sg.student_user_id, 'GRADE' as type, sg.mark as points, 0.0 as aux_value,
                       GREATEST(1, (YEAR(sg.exam_date) - g.formation_year - IF(MONTH(sg.exam_date) < 9, 1, 0)) * 2 + IF(MONTH(sg.exam_date) < 9, 2, 1)) AS semester
                FROM student_grades sg JOIN students_info si ON sg.student_user_id = si.user_id JOIN `groups` g ON si.group_id = g.id
                
                UNION ALL
                
                -- 2. Achievements (aux_value = 0)
                SELECT a.id, a.student_user_id, 'ACHIEVEMENT', a.points_awarded, 0.0,
                       GREATEST(1, (YEAR(a.achievement_date) - g.formation_year - IF(MONTH(a.achievement_date) < 9, 1, 0)) * 2 + IF(MONTH(a.achievement_date) < 9, 2, 1))
                FROM achievements a JOIN students_info si ON a.student_user_id = si.user_id JOIN `groups` g ON si.group_id = g.id
                
                UNION ALL
                
                -- 3. Absences (points = penalty, aux_value = hours)
                SELECT sa.id, sa.student_user_id, 
                       CASE WHEN sa.reason_type = 'EXCUSED' THEN 'ABSENCE_EXCUSED' ELSE 'ABSENCE_UNEXCUSED' END, 
                       sa.hours * 0.1, 
                       sa.hours, 
                       GREATEST(1, (YEAR(sa.absence_date) - g.formation_year - IF(MONTH(sa.absence_date) < 9, 1, 0)) * 2 + IF(MONTH(sa.absence_date) < 9, 2, 1))
                FROM student_absences sa JOIN students_info si ON sa.student_user_id = si.user_id JOIN `groups` g ON si.group_id = g.id
            ),
            Aggregated AS (
                SELECT student_user_id,
                       AVG(CASE WHEN type = 'GRADE' THEN points END) as academic_score,
                       SUM(CASE WHEN type = 'ACHIEVEMENT' THEN points ELSE 0 END) as extracurricular_score,
                       SUM(CASE WHEN type = 'ABSENCE_UNEXCUSED' THEN points ELSE 0 END) as absence_penalty,
                       SUM(CASE WHEN type = 'ABSENCE_UNEXCUSED' THEN aux_value ELSE 0 END) as unexcused_hours,
                       SUM(CASE WHEN type = 'ABSENCE_EXCUSED' THEN aux_value ELSE 0 END) as excused_hours
                FROM SemesterData
                WHERE semester = :semester
                GROUP BY student_user_id
            )
            SELECT u.id AS studentId, u.full_name AS fullName, g.name AS groupName,
                COALESCE(agg.academic_score, 0) AS academicScore,
                COALESCE(agg.extracurricular_score, 0) AS extracurricularScore,
                COALESCE(agg.absence_penalty, 0) AS absencePenalty,
                COALESCE(agg.unexcused_hours, 0) AS unexcusedAbsenceHours,
                COALESCE(agg.excused_hours, 0) AS excusedAbsenceHours,
                (COALESCE(agg.academic_score, 0) + COALESCE(agg.extracurricular_score, 0) - COALESCE(agg.absence_penalty, 0)) AS totalScore
            FROM users u
            JOIN students_info si ON u.id = si.user_id
            JOIN `groups` g ON si.group_id = g.id
            LEFT JOIN Aggregated agg ON u.id = agg.student_user_id
            WHERE u.role_id = (SELECT id FROM roles WHERE name = 'STUDENT')
                AND (:facultyId IS NULL OR g.faculty_id = :facultyId)
                AND (:formationYear IS NULL OR g.formation_year = :formationYear)
                AND (:groupId IS NULL OR g.id = :groupId)
            ORDER BY totalScore DESC
            """, nativeQuery = true)
    List<StudentRankingDto> getStudentRankingListBySemester(
            @Param("facultyId") Integer facultyId,
            @Param("formationYear") Integer formationYear,
            @Param("groupId") Integer groupId,
            @Param("semester") Long semester
    );

    @Query(value = """
            SELECT DISTINCT GREATEST(1, (YEAR(sg.exam_date) - g.formation_year - IF(MONTH(sg.exam_date) < 9, 1, 0)) * 2 + IF(MONTH(sg.exam_date) < 9, 2, 1)) AS semester
            FROM student_grades sg
            JOIN students_info si ON sg.student_user_id = si.user_id
            JOIN `groups` g ON si.group_id = g.id
            WHERE (:groupId IS NULL OR g.id = :groupId)
            ORDER BY semester ASC
            """, nativeQuery = true)
    List<Long> getAvailableSemesters(@Param("groupId") Integer groupId);

    @Query(value = """
            SELECT AVG(sg.mark) FROM student_grades sg WHERE sg.student_user_id = :studentId
            """, nativeQuery = true)
    BigDecimal getStudentAverageAcademicScore(@Param("studentId") Integer studentId);

    @Query(value = """
            WITH AllActivitiesWithSemester AS (
                SELECT
                    GREATEST(1, (YEAR(sg.exam_date) - g.formation_year - IF(MONTH(sg.exam_date) < 9, 1, 0)) * 2 + IF(MONTH(sg.exam_date) < 9, 2, 1)) AS semester,
                    'ACADEMIC' as type, sg.mark as points
                FROM student_grades sg JOIN students_info si ON sg.student_user_id = si.user_id JOIN `groups` g ON si.group_id = g.id
                WHERE sg.student_user_id = :studentId
                UNION ALL
                SELECT
                    GREATEST(1, (YEAR(a.achievement_date) - g.formation_year - IF(MONTH(a.achievement_date) < 9, 1, 0)) * 2 + IF(MONTH(a.achievement_date) < 9, 2, 1)) AS semester,
                    'ACHIEVEMENT' as type, a.points_awarded as points
                FROM achievements a JOIN students_info si ON a.student_user_id = si.user_id JOIN `groups` g ON si.group_id = g.id
                WHERE a.student_user_id = :studentId
                UNION ALL
                SELECT
                    GREATEST(1, (YEAR(sa.absence_date) - g.formation_year - IF(MONTH(sa.absence_date) < 9, 1, 0)) * 2 + IF(MONTH(sa.absence_date) < 9, 2, 1)) AS semester,
                    CASE WHEN sa.reason_type = 'EXCUSED' THEN 'ABSENCE_EXCUSED' ELSE 'ABSENCE_UNEXCUSED' END as type,
                    sa.hours as points
                FROM student_absences sa JOIN students_info si ON sa.student_user_id = si.user_id JOIN `groups` g ON si.group_id = g.id
                WHERE sa.student_user_id = :studentId AND (:absenceType IS NULL OR sa.reason_type = :absenceType)
            )
            SELECT
                semester,
                AVG(CASE WHEN type = 'ACADEMIC' THEN points END) as academicScoreInSemester,
                SUM(CASE WHEN type = 'ACHIEVEMENT' THEN points ELSE 0 END) as achievementsInSemester,
                SUM(CASE WHEN type = 'ABSENCE_UNEXCUSED' THEN -points * 0.1 ELSE 0 END) as absencePenaltyInSemester,
                SUM(CASE WHEN type = 'ABSENCE_UNEXCUSED' THEN points ELSE 0 END) as unexcusedHoursInSemester,
                SUM(CASE WHEN type = 'ABSENCE_EXCUSED' THEN points ELSE 0 END) as excusedHoursInSemester
            FROM AllActivitiesWithSemester GROUP BY semester ORDER BY semester ASC
            """, nativeQuery = true)
    List<DynamicsDetailDto> getStudentDynamicsDetails(@Param("studentId") Integer studentId, @Param("absenceType") String absenceType);

    List<StudentGrade> findByStudentIdAndSubjectIdAndTeacherIdOrderByExamDateDesc(Integer studentId, Integer subjectId, Integer teacherId);

    @Query(value = """
            SELECT g.formation_year AS year, COUNT(DISTINCT u.id) AS studentCount, AVG(sg.mark) AS averageMark
            FROM student_grades sg JOIN users u ON sg.student_user_id = u.id JOIN students_info si ON u.id = si.user_id JOIN `groups` g ON si.group_id = g.id
            GROUP BY g.formation_year ORDER BY year ASC
            """, nativeQuery = true)
    List<EnrollmentDynamicsDto> getEnrollmentDynamics();

    @Query(value = """
            SELECT u.id AS studentId, u.full_name AS studentFullName, g.name AS groupName, AVG(sg.mark) AS averageMark
            FROM student_grades sg JOIN users u ON sg.student_user_id = u.id JOIN students_info si ON u.id = si.user_id JOIN `groups` g ON si.group_id = g.id
            WHERE sg.teacher_user_id = :teacherId GROUP BY u.id, u.full_name, g.name ORDER BY averageMark DESC
            """, nativeQuery = true)
    List<StudentPerformanceDto> getStudentPerformanceForTeacher(@Param("teacherId") Integer teacherId);

    @Query(value = """
            SELECT g.name AS groupName, AVG(sg.mark) AS averageMark
            FROM student_grades sg JOIN students_info si ON sg.student_user_id = si.user_id JOIN `groups` g ON si.group_id = g.id
            WHERE sg.teacher_user_id = :teacherId GROUP BY g.id, g.name ORDER BY averageMark DESC
            """, nativeQuery = true)
    List<ComparisonItemDto> getGroupComparisonForTeacher(@Param("teacherId") Integer teacherId);

    @Query(value = """
            SELECT f.name AS facultyName, AVG(sg.mark) AS averageMark
            FROM student_grades sg JOIN users u ON sg.student_user_id = u.id JOIN students_info si ON u.id = si.user_id JOIN `groups` g ON si.group_id = g.id JOIN faculties f ON g.faculty_id = f.id
            WHERE sg.assessment_type IN ('EXAM', 'DIFFERENTIATED_CREDIT', 'COURSEWORK') GROUP BY f.id, f.name ORDER BY averageMark DESC
            """, nativeQuery = true)
    List<FacultyComparisonDto> getFacultyPerformanceComparison();

    // --- BREAKDOWN QUERY ---
    @Query(value = """
            SELECT semester, category, points 
            FROM (
                SELECT 
                    GREATEST(1, (YEAR(sg.exam_date) - g.formation_year - IF(MONTH(sg.exam_date) < 9, 1, 0)) * 2 + IF(MONTH(sg.exam_date) < 9, 2, 1)) AS semester,
                    'ACADEMIC' as category, AVG(sg.mark) as points
                FROM student_grades sg JOIN students_info si ON sg.student_user_id = si.user_id JOIN `groups` g ON si.group_id = g.id
                WHERE sg.student_user_id = :studentId GROUP BY semester
                UNION ALL
                SELECT 
                    GREATEST(1, (YEAR(a.achievement_date) - g.formation_year - IF(MONTH(a.achievement_date) < 9, 1, 0)) * 2 + IF(MONTH(a.achievement_date) < 9, 2, 1)) AS semester,
                    at.category as category, SUM(a.points_awarded) as points
                FROM achievements a JOIN achievement_types at ON a.type_id = at.id JOIN students_info si ON a.student_user_id = si.user_id JOIN `groups` g ON si.group_id = g.id
                WHERE a.student_user_id = :studentId GROUP BY semester, at.category
            ) as combined_data ORDER BY semester ASC, category
            """, nativeQuery = true)
    List<Object[]> getDetailedBreakdownBySemester(@Param("studentId") Integer studentId);

    Optional<StudentGrade> findFirstByStudentIdAndSubjectIdAndExamDate(Integer studentId, Integer subjectId, LocalDate examDate);

    @Query("SELECT COALESCE(AVG(sg.mark), 0.0) " +
            "FROM StudentGrade sg " +
            "WHERE sg.student.id = :studentId AND sg.subject.id = :subjectId")
    BigDecimal getAverageMarkByStudentAndSubject(
            @Param("studentId") Integer studentId,
            @Param("subjectId") Integer subjectId);
    // (Опционально) Метод поиска по дате, который мы делали ранее:
}
