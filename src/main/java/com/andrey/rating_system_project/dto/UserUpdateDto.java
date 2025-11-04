package com.andrey.rating_system_project.dto;

import lombok.Data;

@Data
public class UserUpdateDto {
    // Поля, которые можно обновить. Они необязательные.
    private String fullName;
    private String email;
    private Integer groupId;      // Для студентов
    private Integer departmentId; // Для преподавателей
    private Integer facultyId;    // Для сотрудников деканата
}