package com.andrey.rating_system_project.dto;

import com.andrey.rating_system_project.model.Specialty;
import lombok.Data;

@Data
public class SpecialtyDto {
    private Integer id;
    private String name;
    private Integer facultyId;

    public SpecialtyDto(Specialty specialty) {
        this.id = specialty.getId();
        this.name = specialty.getName();
        if (specialty.getFaculty() != null) {
            this.facultyId = specialty.getFaculty().getId();
        }
    }
}