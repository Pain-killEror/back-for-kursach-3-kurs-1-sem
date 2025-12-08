package com.andrey.rating_system_project.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class SecurityExceptionHandler {

    // НОВЫЙ ОБРАБОТЧИК ДЛЯ "ВНУТРЕННИХ" ОШИБОК АУТЕНТИФИКАЦИИ
    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<Map<String, String>> handleInternalAuthenticationServiceException(InternalAuthenticationServiceException ex) {
        // Проверяем, какая ошибка была внутри
        Throwable cause = ex.getCause();
        if (cause instanceof DisabledException) {
            return handleDisabledException((DisabledException) cause);
        }
        if (cause instanceof UsernameNotFoundException) {
            return handleUsernameNotFoundException((UsernameNotFoundException) cause);
        }
        // Если внутри что-то другое, возвращаем общую ошибку
        return new ResponseEntity<>(Map.of("message", "Неверный логин или пароль."), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Map<String, String>> handleDisabledException(DisabledException ex) {
        return new ResponseEntity<>(Map.of("message", "Ваша учетная запись заблокирована."), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        if ("User has not been approved yet".equals(ex.getMessage())) {
            return new ResponseEntity<>(Map.of("message", "Учетная запись ожидает подтверждения администратором."), HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(Map.of("message", "Неверный логин или пароль."), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentialsException(BadCredentialsException ex) {
        return new ResponseEntity<>(Map.of("message", "Неверный логин или пароль."), HttpStatus.UNAUTHORIZED);
    }
}