package com.andrey.rating_system_project.controller;

import com.andrey.rating_system_project.dto.SubjectDto;
import com.andrey.rating_system_project.service.SubjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping
    public ResponseEntity<List<SubjectDto>> getAllSubjects() {
        List<SubjectDto> dtos = subjectService.findAll().stream()
                .map(SubjectDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}