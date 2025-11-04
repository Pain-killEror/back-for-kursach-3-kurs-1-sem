package com.andrey.rating_system_project.repository;

import com.andrey.rating_system_project.model.SystemLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {
    // Метод для получения N последних записей
    List<SystemLog> findTop10ByOrderByCreatedAtDesc();
}
