package com.andrey.rating_system_project.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "achievements")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_user_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private AchievementType type;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "achievement_date", nullable = false)
    private LocalDate achievementDate;

    @Column(name = "points_awarded", nullable = false, precision = 5, scale = 2)
    private BigDecimal pointsAwarded;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "added_by_user_id")
    private User addedBy;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;
}