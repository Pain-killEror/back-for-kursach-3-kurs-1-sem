// config/CustomAuthenticationEntryPoint.java
package com.andrey.rating_system_project.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8"); // Указываем кодировку здесь

        Map<String, Object> data = new HashMap<>();
        data.put("status", HttpStatus.UNAUTHORIZED.value());

        // Определяем причину ошибки
        String message;
        if (authException instanceof DisabledException) {
            message = "Ваша учетная запись заблокирована.";
        } else if (authException.getCause() instanceof UsernameNotFoundException && "User has not been approved yet".equals(authException.getCause().getMessage())) {
            // Специально ловим случай, когда пользователь не подтвержден
            message = "Учетная запись ожидает подтверждения администратором.";
        } else if (authException instanceof BadCredentialsException) {
            message = "Неверный логин или пароль.";
        } else {
            // Общая ошибка, если что-то пошло не так
            message = "Ошибка аутентификации.";
        }
        data.put("message", message);

        // Используем getWriter() для правильной работы с кодировкой
        response.getWriter().write(objectMapper.writeValueAsString(data));
    }
}