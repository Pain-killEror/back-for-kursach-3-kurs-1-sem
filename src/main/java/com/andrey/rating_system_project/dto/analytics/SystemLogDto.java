package com.andrey.rating_system_project.dto.analytics;

import com.andrey.rating_system_project.model.SystemLog;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SystemLogDto {
    private String actionType;
    private String description;
    private LocalDateTime createdAt;
    private String userLogin;

    public SystemLogDto(SystemLog log) {
        this.actionType = log.getActionType();
        this.description = log.getDescription();
        this.createdAt = log.getCreatedAt();
        // Безопасно получаем логин, даже если пользователь null
        this.userLogin = (log.getUser() != null) ? log.getUser().getLogin() : "System";
    }
}