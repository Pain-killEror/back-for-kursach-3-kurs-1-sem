package com.andrey.rating_system_project.dto;

import com.andrey.rating_system_project.model.StudentInfo;
import com.andrey.rating_system_project.model.User;
import com.andrey.rating_system_project.model.enums.UserStatus;
import lombok.Data;

@Data
public class UserResponseDto {
    private Integer id;
    private String login;
    private String fullName;
    private String email;
    private UserStatus status;
    private String roleName;
    // Информация о студенте
    private String groupName;
    private String specialtyName;

    // Информация о сотруднике/студенте
    private String facultyName;
    private Integer facultyId; // <--- ДОБАВИЛИ ЭТО ПОЛЕ

    public UserResponseDto(User user) {
        this.id = user.getId();
        this.login = user.getLogin();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.status = user.getStatus();
        if (user.getRole() != null) {
            this.roleName = user.getRole().getName();
        }

        // Логика для студента
        if (user.getStudentInfo() != null) {
            StudentInfo info = user.getStudentInfo();
            if (info.getGroup() != null) {
                this.groupName = info.getGroup().getName();
                if (info.getGroup().getSpecialty() != null) {
                    this.specialtyName = info.getGroup().getSpecialty().getName();
                    if (info.getGroup().getSpecialty().getFaculty() != null) {
                        this.facultyName = info.getGroup().getSpecialty().getFaculty().getName();
                        this.facultyId = info.getGroup().getSpecialty().getFaculty().getId(); // <--- Заполняем ID
                    }
                }
            }
        }
        // Логика для сотрудника деканата (привязка напрямую к факультету)
        else if (user.getFaculty() != null) {
            this.facultyName = user.getFaculty().getName();
            this.facultyId = user.getFaculty().getId(); // <--- Заполняем ID
        }
    }
}