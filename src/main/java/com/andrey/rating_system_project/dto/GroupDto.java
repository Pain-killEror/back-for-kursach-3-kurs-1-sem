package com.andrey.rating_system_project.dto;

import com.andrey.rating_system_project.model.Group;
import lombok.Data;

@Data
public class GroupDto {
    private Integer id;
    private String name;
    private Integer formationYear;
    private Integer facultyId;
    private String facultyName;

    public GroupDto(Group group) {
        this.id = group.getId();
        this.name = group.getName();
        this.formationYear = group.getFormationYear();
        if (group.getFaculty() != null) {
            this.facultyId = group.getFaculty().getId();
            this.facultyName = group.getFaculty().getName();
        }
    }
}