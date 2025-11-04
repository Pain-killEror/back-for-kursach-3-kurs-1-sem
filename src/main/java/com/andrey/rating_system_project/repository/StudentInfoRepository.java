package com.andrey.rating_system_project.repository;

import com.andrey.rating_system_project.dto.analytics.StatItemDto;
import com.andrey.rating_system_project.model.StudentInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentInfoRepository extends JpaRepository<StudentInfo, Integer> {


        // НОВЫЙ МЕТОД: Подсчет студентов по форме обучения
        @Query("SELECT new com.andrey.rating_system_project.dto.analytics.StatItemDto(CAST(si.educationForm AS string), COUNT(si.id)) " +
                "FROM StudentInfo si GROUP BY si.educationForm")
        List<StatItemDto> countByEducationForm();

}
