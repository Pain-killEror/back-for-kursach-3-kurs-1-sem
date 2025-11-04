package com.andrey.rating_system_project.controller;

import com.andrey.rating_system_project.dto.GradeCreateDto;
import com.andrey.rating_system_project.model.StudentGrade;
import com.andrey.rating_system_project.service.GradeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/grades")
//@CrossOrigin(origins = "*")
public class GradeController {

    private final GradeService gradeService;

    public GradeController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<StudentGrade> addGrade(@Valid @RequestBody GradeCreateDto gradeDto) {
        StudentGrade newGrade = gradeService.addGrade(gradeDto);
        return new ResponseEntity<>(newGrade, HttpStatus.CREATED);
    }
}