package com.andrey.rating_system_project.controller;

import com.andrey.rating_system_project.dto.AchievementCreateDto;
import com.andrey.rating_system_project.model.AchievementType;
import com.andrey.rating_system_project.service.AchievementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/achievements")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;

    // Эндпоинт для получения всех типов достижений
    @GetMapping("/types")
    @PreAuthorize("hasAnyAuthority('DEAN_STAFF', 'ADMINISTRATOR')")
    public ResponseEntity<List<AchievementType>> getAllAchievementTypes() {
        return ResponseEntity.ok(achievementService.getAllTypes());
    }

    // Эндпоинт для добавления нового достижения студенту
    @PostMapping
    @PreAuthorize("hasAnyAuthority('DEAN_STAFF', 'ADMINISTRATOR')")
    public ResponseEntity<?> addAchievementForStudent(@RequestBody AchievementCreateDto createDto) {
        achievementService.addAchievement(createDto);
        return ResponseEntity.ok().build();
    }
}