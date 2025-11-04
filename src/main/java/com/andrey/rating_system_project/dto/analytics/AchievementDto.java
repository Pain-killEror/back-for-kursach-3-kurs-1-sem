package com.andrey.rating_system_project.dto.analytics;

import com.andrey.rating_system_project.model.Achievement;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AchievementDto {
    private String typeName;
    private BigDecimal pointsAwarded;
    private Integer studentId;
    private LocalDateTime createdAt;

    public AchievementDto(Achievement achievement) {
        this.typeName = achievement.getType().getName();
        this.pointsAwarded = achievement.getPointsAwarded();
        this.studentId = achievement.getStudent().getId();
        this.createdAt = achievement.getCreatedAt();
    }
}