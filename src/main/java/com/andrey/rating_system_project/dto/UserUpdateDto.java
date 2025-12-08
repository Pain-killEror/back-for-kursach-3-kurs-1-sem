package com.andrey.rating_system_project.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserUpdateDto {
    // Поля, которые можно обновить. Они необязательные.
    private String fullName;
    private String email;

    private Integer groupId;      // Для студентов
    private Integer facultyId;    // Для сотрудников деканата

    // НОВОЕ ПОЛЕ: Список ID предметов для преподавателя
    private List<Integer> subjectIds;
}