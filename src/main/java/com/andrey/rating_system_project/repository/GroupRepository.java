package com.andrey.rating_system_project.repository;

import com.andrey.rating_system_project.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Integer> {

    // Существующий метод для поиска всех групп факультета
    List<Group> findByFacultyId(Integer facultyId);

    // НОВЫЙ МЕТОД: Поиск групп по Факультету, Специальности и Году формирования
    @Query("SELECT g FROM Group g WHERE g.faculty.id = :facultyId AND g.specialty.id = :specialtyId AND g.formationYear = :year")
    List<Group> findByFacultyAndSpecialtyAndYear(
            @Param("facultyId") Integer facultyId,
            @Param("specialtyId") Integer specialtyId,
            @Param("year") Integer year
    );

    // НОВЫЙ МЕТОД: Подсчет студентов в группе
    @Query("SELECT COUNT(si) FROM StudentInfo si WHERE si.group.id = :groupId")
    Long countStudentsInGroup(@Param("groupId") Integer groupId);
}