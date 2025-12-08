package com.andrey.rating_system_project.controller;

import com.andrey.rating_system_project.dto.GroupDto;
import com.andrey.rating_system_project.dto.GroupWithCountDto;
import com.andrey.rating_system_project.model.Group;
import com.andrey.rating_system_project.service.GroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping
    public ResponseEntity<List<GroupDto>> getAllGroups() {
        List<GroupDto> dtos = groupService.findAll().stream()
                .map(GroupDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/faculty/{facultyId}")
    public ResponseEntity<List<GroupDto>> getGroupsByFaculty(@PathVariable Integer facultyId) {
        List<GroupDto> dtos = groupService.findGroupsByFacultyId(facultyId).stream()
                .map(GroupDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/available")
    public ResponseEntity<List<GroupWithCountDto>> getAvailableGroups(
            @RequestParam Integer facultyId,
            @RequestParam Integer specialtyId,
            @RequestParam Integer course
    ) {
        return ResponseEntity.ok(groupService.findAvailableGroups(facultyId, specialtyId, course));
    }

    // --- НОВЫЙ ЭНДПОИНТ: Автоматическое создание группы ---
    @PostMapping("/auto-create")
    public ResponseEntity<GroupDto> createAutoGroup(
            @RequestParam Integer facultyId,
            @RequestParam Integer specialtyId,
            @RequestParam Integer course
    ) {
        Group newGroup = groupService.createAutoGroup(facultyId, specialtyId, course);
        return ResponseEntity.ok(new GroupDto(newGroup));
    }
}