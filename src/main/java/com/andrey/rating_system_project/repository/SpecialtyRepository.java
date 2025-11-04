package com.andrey.rating_system_project.repository;

import com.andrey.rating_system_project.model.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecialtyRepository extends JpaRepository<Specialty, Integer> {
}