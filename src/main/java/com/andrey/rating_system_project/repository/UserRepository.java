package com.andrey.rating_system_project.repository;

import com.andrey.rating_system_project.dto.analytics.StatItemDto;
import com.andrey.rating_system_project.dto.analytics.StudentShortDto;
import com.andrey.rating_system_project.model.User;
import com.andrey.rating_system_project.model.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    boolean existsByLogin(String login);

    boolean existsByEmail(String email);

    Optional<User> findByLogin(String login);

    // 1. JPQL с конструктором (StudentShortDto)
    @Query("SELECT new com.andrey.rating_system_project.dto.analytics.StudentShortDto(u.id, u.fullName) " +
            "FROM User u " +
            "JOIN u.studentInfo si " +
            "WHERE si.group.id = :groupId " +
            "ORDER BY u.fullName")
    List<StudentShortDto> findStudentsByGroupId(@Param("groupId") Integer groupId);

    // 2. JPQL с конструктором (StatItemDto)
    @Query("SELECT new com.andrey.rating_system_project.dto.analytics.StatItemDto(r.name, COUNT(u.id)) " +
            "FROM User u JOIN u.role r GROUP BY r.name")
    List<StatItemDto> countUsersByRole();

    // 3. JPQL с конструктором (StatItemDto)
    // CAST(u.status as string) может вызывать вопросы у старых версий Hibernate,
    // но обычно работает. Если будет ошибка, можно просто выбрать u.status
    @Query("SELECT new com.andrey.rating_system_project.dto.analytics.StatItemDto(CAST(u.status AS string), COUNT(u.id)) " +
            "FROM User u GROUP BY u.status")
    List<StatItemDto> countUsersByStatus();

    // 4. NATIVE QUERY (Нативный SQL)
    // ВНИМАНИЕ: Нативный запрос вернет Object[], если не использовать интерфейс.
    // Оставляем так, но нам придется маппить это вручную в сервисе,
    // ЛИБО используйте интерфейс-проекцию (см. ниже).
    // Пока оставим List<StatItemDto>, но это может упасть в Runtime.
    // Если упадет, замените возвращаемый тип на List<Object[]> или создайте интерфейс.
    @Query(value = "SELECT DATE_FORMAT(created_at, '%Y-%m-%d') as label, COUNT(id) as count " +
            "FROM users " +
            "WHERE created_at >= CURDATE() - INTERVAL 7 DAY " +
            "GROUP BY label " +
            "ORDER BY label ASC", nativeQuery = true)
    List<Object[]> countRegistrationsLast7DaysRaw();
    // Я переименовал и изменил возвращаемый тип на Object[], чтобы это точно работало.
    // В сервисе вы сделаете map: list.stream().map(obj -> new StatItemDto((String)obj[0], ((Number)obj[1]).longValue()))...

    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.role " +
            "LEFT JOIN FETCH u.faculty " +
            "LEFT JOIN FETCH u.studentInfo si " +
            "LEFT JOIN FETCH si.group g " +
            "LEFT JOIN FETCH g.specialty s " +
            "LEFT JOIN FETCH s.faculty " +
            "WHERE u.status IN :statuses")
    List<User> findByStatusInWithDetails(@Param("statuses") List<UserStatus> statuses);
}