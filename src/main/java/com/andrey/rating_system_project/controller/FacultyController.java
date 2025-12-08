package com.andrey.rating_system_project.controller;

import com.andrey.rating_system_project.dto.FacultyDto; // Импорт DTO
import com.andrey.rating_system_project.service.FacultyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/faculties")
public class FacultyController {

    private final FacultyService facultyService;

    public FacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @GetMapping
    public ResponseEntity<List<FacultyDto>> getAllFaculties() { // Возвращаем DTO
        List<FacultyDto> dtos = facultyService.findAll().stream()
                .map(FacultyDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}