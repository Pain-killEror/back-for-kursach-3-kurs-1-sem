package com.andrey.rating_system_project.dto;

import lombok.Data;

@Data
public class AuthRequestDto {
    private String login;
    private String password;
}