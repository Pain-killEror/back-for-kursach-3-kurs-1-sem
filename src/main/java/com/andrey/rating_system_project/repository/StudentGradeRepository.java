package com.andrey.rating_system_project.repository;

import com.andrey.rating_system_project.dto.analytics.*;
import com.andrey.rating_system_project.model.StudentGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import com.andrey.rating_system_project.dto.analytics.StudentPerformanceDto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface StudentGradeRepository extends JpaRepository<StudentGrade, Long> {

    @Query(value = """
        WITH StudentAverage AS (
            SELECT
                sg.student_user_id,
                AVG(sg.mark) as avg_mark
            FROM student_grades sg
            JOIN users u ON sg.student_user_id = u.id
            JOIN students_info si ON u.id = si.user_id
            JOIN `groups` g ON si.group_id = g.id
            WHERE
                (:assessmentTypes IS NULL OR sg.assessment_type IN (:assessmentTypes))
                AND (:facultyId IS NULL OR g.faculty_id = :facultyId)
                AND (:formationYear IS NULL OR g.formation_year = :formationYear)
                AND (:groupId IS NULL OR g.id = :groupId)
                AND (:educationForm IS NULL OR si.education_form = :educationForm)
            GROUP BY sg.student_user_id
        )
        SELECT
            CASE
                WHEN avg_mark >= 9 THEN '9-10'
                WHEN avg_mark >= 8 THEN '8-9'
                WHEN avg_mark >= 7 THEN '7-8'
                WHEN avg_mark >= 6 THEN '6-7'
                WHEN avg_mark >= 5 THEN '5-6'
                WHEN avg_mark >= 4 THEN '4-5'
                ELSE '0-4'
            END AS label,
            COUNT(*) as count
        FROM StudentAverage
        GROUP BY label
        ORDER BY label DESC
    """, nativeQuery = true)
    List<DistributionItemDto> getPerformanceDistribution(
            @Param("facultyId") Integer facultyId,
            @Param("formationYear") Integer formationYear,
            @Param("groupId") Integer groupId,
            @Param("assessmentTypes") List<String> assessmentTypes,
            @Param("educationForm") String educationForm
    );

    @Query(value = """
        SELECT
            (YEAR(sg.exam_date) - g.formation_year - IF(MONTH(sg.exam_date) < 9, 1, 0)) * 2 + IF(MONTH(sg.exam_date) < 9, 2, 1) AS semester,
            AVG(sg.mark) as averageMark
        FROM student_grades sg
        JOIN users u ON sg.student_user_id = u.id
        JOIN students_info si ON u.id = si.user_id
        JOIN `groups` g ON si.group_id = g.id
        WHERE
            (:assessmentTypes IS NULL OR sg.assessment_type IN (:assessmentTypes))
            AND (:facultyId IS NULL OR g.faculty_id = :facultyId)
            AND (:formationYear IS NULL OR g.formation_year = :formationYear)
            AND (:groupId IS NULL OR g.id = :groupId)
            AND (:educationForm IS NULL OR si.education_form = :educationForm)
        GROUP BY semester
        ORDER BY semester ASC
    """, nativeQuery = true)
    List<DynamicsPointDto> getAverageScoreDynamics(
            @Param("facultyId") Integer facultyId,
            @Param("formationYear") Integer formationYear,
            @Param("groupId") Integer groupId,
            @Param("assessmentTypes") List<String> assessmentTypes,
            @Param("educationForm") String educationForm
    );

    @Query(value = """
        WITH StudentAverage AS (
            SELECT
                sg.student_user_id,
                AVG(sg.mark) as avg_mark
            FROM student_grades sg
            JOIN users u ON sg.student_user_id = u.id
            JOIN students_info si ON u.id = si.user_id
            JOIN `groups` g ON si.group_id = g.id
            WHERE
                (:assessmentTypes IS NULL OR sg.assessment_type IN (:assessmentTypes))
                AND (:facultyId IS NULL OR g.faculty_id = :facultyId)
                AND (:formationYear IS NULL OR g.formation_year = :formationYear)
                AND (:groupId IS NULL OR g.id = :groupId)
                AND (:educationForm IS NULL OR si.education_form = :educationForm)
            GROUP BY sg.student_user_id
        )
        SELECT
            AVG(avg_mark) AS overallAverageMark,
            COUNT(*) AS totalStudents,
            (SUM(CASE WHEN avg_mark < 4 THEN 1 ELSE 0 END) * 100.0 / COUNT(*)) AS failingStudentsPercentage,
            (SUM(CASE WHEN avg_mark >= 9 THEN 1 ELSE 0 END) * 100.0 / COUNT(*)) AS excellentStudentsPercentage
        FROM StudentAverage
    """, nativeQuery = true)
    KeyMetricsDto getKeyMetrics(
            @Param("facultyId") Integer facultyId,
            @Param("formationYear") Integer formationYear,
            @Param("groupId") Integer groupId,
            @Param("assessmentTypes") List<String> assessmentTypes,
            @Param("educationForm") String educationForm
    );

    @Query(value = """
        SELECT
            g.name AS groupName,
            AVG(sg.mark) AS averageMark
        FROM student_grades sg
        JOIN users u ON sg.student_user_id = u.id
        JOIN students_info si ON u.id = si.user_id
        JOIN `groups` g ON si.group_id = g.id
        WHERE
            (:assessmentTypes IS NULL OR sg.assessment_type IN (:assessmentTypes))
            AND (:facultyId IS NULL OR g.faculty_id = :facultyId)
            AND (:formationYear IS NULL OR g.formation_year = :formationYear)
            AND (:educationForm IS NULL OR si.education_form = :educationForm)
        GROUP BY g.id, g.name
        ORDER BY averageMark DESC
    """, nativeQuery = true)
    List<ComparisonItemDto> getGroupComparison(
            @Param("facultyId") Integer facultyId,
            @Param("formationYear") Integer formationYear,
            @Param("assessmentTypes") List<String> assessmentTypes,
            @Param("educationForm") String educationForm
    );

    @Query(value = """
        SELECT SUM(sg.mark)
        FROM student_grades sg
        JOIN users u ON sg.student_user_id = u.id
        JOIN students_info si ON u.id = si.user_id
        JOIN `groups` g ON si.group_id = g.id
        WHERE
            (:assessmentTypes IS NULL OR sg.assessment_type IN (:assessmentTypes))
            AND (:facultyId IS NULL OR g.faculty_id = :facultyId)
            AND (:formationYear IS NULL OR g.formation_year = :formationYear)
            AND (:groupId IS NULL OR g.id = :groupId)
            AND (:educationForm IS NULL OR si.education_form = :educationForm)
    """, nativeQuery = true)
    BigDecimal getTotalAcademicPoints(
            @Param("facultyId") Integer facultyId,
            @Param("formationYear") Integer formationYear,
            @Param("groupId") Integer groupId,
            @Param("assessmentTypes") List<String> assessmentTypes,
            @Param("educationForm") String educationForm
    );

    @Query(value = """
        WITH AcademicScores AS (
            SELECT
                sg.student_user_id AS student_id,
                AVG(sg.mark) AS academic_score
            FROM student_grades sg
            WHERE (:assessmentTypes IS NULL OR sg.assessment_type IN (:assessmentTypes))
            GROUP BY sg.student_user_id
        ),
        ExtracurricularScores AS (
            SELECT
                a.student_user_id AS student_id,
                SUM(a.points_awarded) AS extracurricular_score
            FROM achievements a
            GROUP BY a.student_user_id
        ),
        AbsenceData AS (
            SELECT
                sa.student_user_id AS student_id,
                SUM(CASE WHEN sa.reason_type = 'UNEXCUSED' THEN sa.hours ELSE 0 END) AS unexcused_hours,
                SUM(CASE WHEN sa.reason_type = 'EXCUSED' THEN sa.hours ELSE 0 END) AS excused_hours,
                SUM(CASE WHEN sa.reason_type = 'UNEXCUSED' THEN sa.hours ELSE 0 END) * 0.1 AS absence_penalty
            FROM student_absences sa
            GROUP BY sa.student_user_id
        )
        SELECT
            u.id AS studentId,
            u.full_name AS fullName,
            g.name AS groupName,
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
        WHERE
            u.role_id = (SELECT id FROM roles WHERE name = 'STUDENT')
            AND (:facultyId IS NULL OR g.faculty_id = :facultyId)
            AND (:formationYear IS NULL OR g.formation_year = :formationYear)
            AND (:groupId IS NULL OR g.id = :groupId)
            AND (:educationForm IS NULL OR si.education_form = :educationForm)
        ORDER BY totalScore DESC
    """, nativeQuery = true)
    List<StudentRankingDto> getStudentRankingList(
            @Param("facultyId") Integer facultyId,
            @Param("formationYear") Integer formationYear,
            @Param("groupId") Integer groupId,
            @Param("assessmentTypes") List<String> assessmentTypes,
            @Param("educationForm") String educationForm
    );

    @Query(value = """
        SELECT AVG(sg.mark)
        FROM student_grades sg
        WHERE sg.student_user_id = :studentId
        AND sg.assessment_type IN ('EXAM', 'DIFFERENTIATED_CREDIT', 'COURSEWORK')
    """, nativeQuery = true)
    BigDecimal getStudentAverageAcademicScore(@Param("studentId") Integer studentId);

    @Query(value = """
        WITH AllActivitiesWithSemester AS (
            SELECT
                (YEAR(sg.exam_date) - g.formation_year - IF(MONTH(sg.exam_date) < 9, 1, 0)) * 2 + IF(MONTH(sg.exam_date) < 9, 2, 1) AS semester,
                'ACADEMIC' as type,
                sg.mark as points
            FROM student_grades sg
            JOIN students_info si ON sg.student_user_id = si.user_id
            JOIN `groups` g ON si.group_id = g.id
            WHERE sg.student_user_id = :studentId AND sg.assessment_type IN ('EXAM', 'DIFFERENTIATED_CREDIT', 'COURSEWORK')
            UNION ALL
            SELECT
                (YEAR(a.achievement_date) - g.formation_year - IF(MONTH(a.achievement_date) < 9, 1, 0)) * 2 + IF(MONTH(a.achievement_date) < 9, 2, 1) AS semester,
                'ACHIEVEMENT' as type,
                a.points_awarded as points
            FROM achievements a
            JOIN students_info si ON a.student_user_id = si.user_id
            JOIN `groups` g ON si.group_id = g.id
            WHERE a.student_user_id = :studentId
            UNION ALL
            SELECT
                (YEAR(sa.absence_date) - g.formation_year - IF(MONTH(sa.absence_date) < 9, 1, 0)) * 2 + IF(MONTH(sa.absence_date) < 9, 2, 1) AS semester,
                'ABSENCE' as type,
                -sa.hours * 0.1 as points
            FROM student_absences sa
            JOIN students_info si ON sa.student_user_id = si.user_id
            JOIN `groups` g ON si.group_id = g.id
            WHERE sa.student_user_id = :studentId
              AND (:absenceType IS NULL OR sa.reason_type = :absenceType)
        )
        SELECT
            semester,
            AVG(CASE WHEN type = 'ACADEMIC' THEN points END) as academicScoreInSemester,
            SUM(CASE WHEN type = 'ACHIEVEMENT' THEN points ELSE 0 END) as achievementsInSemester,
            SUM(CASE WHEN type = 'ABSENCE' THEN points ELSE 0 END) as absencePenaltyInSemester
        FROM AllActivitiesWithSemester
        GROUP BY semester
        ORDER BY semester ASC
    """, nativeQuery = true)
    List<DynamicsDetailDto> getStudentDynamicsDetails(
            @Param("studentId") Integer studentId,
            @Param("absenceType") String absenceType
    );

    List<StudentGrade> findByStudentIdAndSubjectIdAndTeacherIdOrderByExamDateDesc(Integer studentId, Integer subjectId, Integer teacherId);

    @Query(value = """
        SELECT f.name AS facultyName, AVG(sg.mark) AS averageMark
        FROM student_grades sg
        JOIN users u ON sg.student_user_id = u.id
        JOIN students_info si ON u.id = si.user_id
        JOIN `groups` g ON si.group_id = g.id
        JOIN faculties f ON g.faculty_id = f.id
        WHERE sg.assessment_type IN ('EXAM', 'DIFFERENTIATED_CREDIT', 'COURSEWORK')
        GROUP BY f.id, f.name
        ORDER BY averageMark DESC
    """, nativeQuery = true)
    List<FacultyComparisonDto> getFacultyPerformanceComparison();

    @Query(value = """
        SELECT
            g.formation_year AS year,
            COUNT(DISTINCT u.id) AS studentCount,
            AVG(sg.mark) AS averageMark
        FROM student_grades sg
        JOIN users u ON sg.student_user_id = u.id
        JOIN students_info si ON u.id = si.user_id
        JOIN `groups` g ON si.group_id = g.id
        GROUP BY g.formation_year
        ORDER BY year ASC
    """, nativeQuery = true)
    List<EnrollmentDynamicsDto> getEnrollmentDynamics();

    // НОВЫЙ МЕТОД 1: Успеваемость студентов для конкретного преподавателя
    @Query(value = """
            SELECT
                u.id AS studentId,
                u.full_name AS studentFullName,
                g.name AS groupName,
                AVG(sg.mark) AS averageMark
            FROM student_grades sg
            JOIN users u ON sg.student_user_id = u.id
            JOIN students_info si ON u.id = si.user_id
            JOIN `groups` g ON si.group_id = g.id
            WHERE sg.teacher_user_id = :teacherId
            GROUP BY u.id, u.full_name, g.name
            ORDER BY averageMark DESC
            """, nativeQuery = true)
    List<StudentPerformanceDto> getStudentPerformanceForTeacher(@Param("teacherId") Integer teacherId);

    // НОВЫЙ МЕТОД 2: Сравнение групп для конкретного преподавателя
    @Query(value = """
            SELECT
                g.name AS groupName,
                AVG(sg.mark) AS averageMark
            FROM student_grades sg
            JOIN students_info si ON sg.student_user_id = si.user_id
            JOIN `groups` g ON si.group_id = g.id
            WHERE sg.teacher_user_id = :teacherId
            GROUP BY g.id, g.name
            ORDER BY averageMark DESC
            """, nativeQuery = true)
    List<ComparisonItemDto> getGroupComparisonForTeacher(@Param("teacherId") Integer teacherId);
}