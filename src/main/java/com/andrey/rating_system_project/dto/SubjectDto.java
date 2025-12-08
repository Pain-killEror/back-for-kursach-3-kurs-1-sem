package com.andrey.rating_system_project.dto;

import com.andrey.rating_system_project.model.Subject;
import lombok.Data;

@Data
public class SubjectDto {
    private Integer id;
    private String name;

    public SubjectDto(Subject subject) {
        this.id = subject.getId();
        this.name = subject.getName();
    }
}