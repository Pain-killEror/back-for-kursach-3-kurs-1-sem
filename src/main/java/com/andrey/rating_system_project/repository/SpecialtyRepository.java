package com.andrey.rating_system_project.repository;

import com.andrey.rating_system_project.model.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpecialtyRepository extends JpaRepository<Specialty, Integer> {

    // Метод для поиска специальностей конкретного факультета
    List<Specialty> findByFacultyId(Integer facultyId);
}