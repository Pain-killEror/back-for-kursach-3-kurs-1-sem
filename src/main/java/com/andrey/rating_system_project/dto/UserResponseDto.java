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

    private String groupName;
    private String specialtyName;
    private String facultyName;

    public UserResponseDto(User user) {
        this.id = user.getId();
        this.login = user.getLogin();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.status = user.getStatus();

        if (user.getRole() != null) {
            this.roleName = user.getRole().getName();
        }

        // Проверяем, является ли пользователь студентом и есть ли у него studentInfo
        if (user.getStudentInfo() != null) {
            StudentInfo info = user.getStudentInfo();
            if (info.getGroup() != null) {
                this.groupName = info.getGroup().getName();
                if (info.getGroup().getSpecialty() != null) {
                    this.specialtyName = info.getGroup().getSpecialty().getName();
                    if (info.getGroup().getSpecialty().getFaculty() != null) {
                        this.facultyName = info.getGroup().getSpecialty().getFaculty().getName();
                    }
                }
            }
        }
    }
}