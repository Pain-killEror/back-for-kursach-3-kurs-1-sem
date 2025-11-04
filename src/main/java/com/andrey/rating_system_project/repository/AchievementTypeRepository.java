package com.andrey.rating_system_project.repository;

import com.andrey.rating_system_project.model.AchievementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AchievementTypeRepository extends JpaRepository<AchievementType, Integer> {
}
