package com.andrey.rating_system_project.controller;

import com.andrey.rating_system_project.dto.SpecialtyDto;
import com.andrey.rating_system_project.service.SpecialtyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/specialties")
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    public SpecialtyController(SpecialtyService specialtyService) {
        this.specialtyService = specialtyService;
    }

    @GetMapping
    public ResponseEntity<List<SpecialtyDto>> getAllSpecialties() {
        List<SpecialtyDto> dtos = specialtyService.findAll().stream()
                .map(SpecialtyDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/faculty/{facultyId}")
    public ResponseEntity<List<SpecialtyDto>> getSpecialtiesByFaculty(@PathVariable Integer facultyId) {
        List<SpecialtyDto> dtos = specialtyService.findSpecialtiesByFacultyId(facultyId).stream()
                .map(SpecialtyDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}