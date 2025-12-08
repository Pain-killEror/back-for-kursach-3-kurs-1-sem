package com.andrey.rating_system_project.dto;

import com.andrey.rating_system_project.model.Faculty;
import lombok.Data;

@Data
public class FacultyDto {
    private Integer id;
    private String name;

    public FacultyDto(Faculty faculty) {
        this.id = faculty.getId();
        this.name = faculty.getName();
    }
}