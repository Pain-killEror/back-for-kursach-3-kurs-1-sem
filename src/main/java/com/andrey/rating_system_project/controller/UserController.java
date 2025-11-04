package com.andrey.rating_system_project.controller;

import com.andrey.rating_system_project.dto.UserCreateDto;
import com.andrey.rating_system_project.dto.UserResponseDto;
import com.andrey.rating_system_project.dto.UserUpdateDto;
import com.andrey.rating_system_project.model.User;
import com.andrey.rating_system_project.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> registerUser(@Valid @RequestBody UserCreateDto userDto) {
        User newUser = userService.registerUser(userDto);
        return new ResponseEntity<>(new UserResponseDto(newUser), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<UserResponseDto> approveUser(@PathVariable Integer id, @RequestParam Integer roleId) {
        User approvedUser = userService.approveUser(id, roleId);
        return ResponseEntity.ok(new UserResponseDto(approvedUser));
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<User> users = userService.findAllUsers();
        List<UserResponseDto> userDtos = users.stream()
                .map(UserResponseDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Integer id) {
        User user = userService.findUserById(id);
        return ResponseEntity.ok(new UserResponseDto(user));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(Authentication authentication) {
        String login = authentication.getName();
        User user = userService.findByLogin(login);
        return ResponseEntity.ok(new UserResponseDto(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Integer id, @RequestBody UserUpdateDto updateDto) {
        User updatedUser = userService.updateUser(id, updateDto);
        return ResponseEntity.ok(new UserResponseDto(updatedUser));
    }

    @PatchMapping("/{studentId}/assign-group")
    public ResponseEntity<?> assignStudentToGroup(@PathVariable Integer studentId, @RequestParam Integer groupId) {
        userService.assignStudentToGroup(studentId, groupId);
        return ResponseEntity.ok().build();
    }
}