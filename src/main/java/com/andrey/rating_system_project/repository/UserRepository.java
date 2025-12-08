package com.andrey.rating_system_project.repository;

import com.andrey.rating_system_project.dto.analytics.StatItemDto;
import com.andrey.rating_system_project.dto.analytics.StudentShortDto;
import com.andrey.rating_system_project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import com.andrey.rating_system_project.model.enums.UserStatus;


@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    boolean existsByLogin(String login);

    boolean existsByEmail(String email);

    Optional<User> findByLogin(String login);

    @Query("SELECT new com.andrey.rating_system_project.dto.analytics.StudentShortDto(u.id, u.fullName) FROM User u JOIN u.studentInfo si WHERE si.group.id = :groupId ORDER BY u.fullName")
    List<StudentShortDto> findStudentsByGroupId(@Param("groupId") Integer groupId);

    @Query("SELECT new com.andrey.rating_system_project.dto.analytics.StatItemDto(r.name, COUNT(u.id)) FROM User u JOIN u.role r GROUP BY r.name")
    List<StatItemDto> countUsersByRole();

    @Query("SELECT new com.andrey.rating_system_project.dto.analytics.StatItemDto(CAST(u.status AS string), COUNT(u.id)) FROM User u GROUP BY u.status")
    List<StatItemDto> countUsersByStatus();

    @Query(value = "SELECT DATE_FORMAT(created_at, '%Y-%m-%d') as label, COUNT(id) as count FROM users WHERE created_at >= CURDATE() - INTERVAL 7 DAY GROUP BY label ORDER BY label ASC", nativeQuery = true)
    List<StatItemDto> countRegistrationsLast7Days();

    //List<User> findByStatusIn(List<UserStatus> statuses);

    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.role " +
            "LEFT JOIN FETCH u.faculty " +
            "LEFT JOIN FETCH u.studentInfo si " +
            "LEFT JOIN FETCH si.group g " +
            "LEFT JOIN FETCH g.specialty s " +
            "LEFT JOIN FETCH s.faculty " +
            "WHERE u.status IN :statuses")
    List<User> findByStatusInWithDetails(List<UserStatus> statuses);
}