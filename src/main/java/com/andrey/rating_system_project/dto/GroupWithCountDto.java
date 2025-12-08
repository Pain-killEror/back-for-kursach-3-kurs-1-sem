package com.andrey.rating_system_project.dto;

import com.andrey.rating_system_project.model.Group;
import lombok.Data;

@Data
public class GroupWithCountDto {
    private Integer id;
    private String name;
    private Long studentCount;
    private boolean isFull; // Флаг: true, если в группе >= 15 человек

    public GroupWithCountDto(Group group, Long studentCount) {
        this.id = group.getId();
        this.name = group.getName();
        this.studentCount = studentCount;
        // Жесткое ограничение в 15 человек, как вы просили
        this.isFull = studentCount >= 15;
    }
}