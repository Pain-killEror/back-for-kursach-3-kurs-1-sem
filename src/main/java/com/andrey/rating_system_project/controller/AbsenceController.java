package com.andrey.rating_system_project.controller;

import com.andrey.rating_system_project.dto.AbsenceCreateDto;
import com.andrey.rating_system_project.service.AbsenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/absences")
@RequiredArgsConstructor
public class AbsenceController {

    private final AbsenceService absenceService;

    @PostMapping
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<Void> addAbsence(@Valid @RequestBody AbsenceCreateDto dto) {
        absenceService.addAbsence(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}