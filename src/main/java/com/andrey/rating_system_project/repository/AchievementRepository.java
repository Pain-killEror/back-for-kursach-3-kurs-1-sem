package com.andrey.rating_system_project.repository;

import com.andrey.rating_system_project.dto.analytics.ContributionItemDto;
import com.andrey.rating_system_project.dto.analytics.FacultyActivityDto;
import com.andrey.rating_system_project.model.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Integer> {


    List<Achievement> findTop5ByAddedByIdOrderByCreatedAtDesc(Integer addedById);
    @Query(value = """
        SELECT
            at.category AS category,
            SUM(a.points_awarded) AS totalPoints
        FROM
            achievements a
        JOIN
            achievement_types at ON a.type_id = at.id
        JOIN
            users u ON a.student_user_id = u.id
        JOIN
            students_info si ON u.id = si.user_id
        JOIN
            `groups` g ON si.group_id = g.id
        WHERE
            (:facultyId IS NULL OR g.faculty_id = :facultyId)
            AND (:formationYear IS NULL OR g.formation_year = :formationYear)
            AND (:groupId IS NULL OR g.id = :groupId)
        GROUP BY
            at.category
    """, nativeQuery = true)
    List<ContributionItemDto> getAchievementsContribution(
            @Param("facultyId") Integer facultyId,
            @Param("formationYear") Integer formationYear,
            @Param("groupId") Integer groupId
    );

    ///


    @Query(value = """
        SELECT
            at.category AS category,
            SUM(a.points_awarded) AS totalPoints
        FROM
            achievements a
        JOIN
            achievement_types at ON a.type_id = at.id
        WHERE
            a.student_user_id = :studentId
        GROUP BY
            at.category
    """, nativeQuery = true)
    List<ContributionItemDto> getStudentAchievementsContribution(@Param("studentId") Integer studentId);



    ///


    // НОВЫЙ МЕТОД: Обзор внеучебной деятельности по факультетам
    @Query(value = """
            SELECT f.name as facultyName, at.category as category, SUM(a.points_awarded) as totalPoints
            FROM achievements a
            JOIN achievement_types at ON a.type_id = at.id
            JOIN users u ON a.student_user_id = u.id
            JOIN students_info si ON u.id = si.user_id
            JOIN `groups` g ON si.group_id = g.id
            JOIN faculties f ON g.faculty_id = f.id
            GROUP BY f.name, at.category
            ORDER BY f.name, at.category
            """, nativeQuery = true)
    List<FacultyActivityDto> getFacultyExtracurricularActivity();

}
