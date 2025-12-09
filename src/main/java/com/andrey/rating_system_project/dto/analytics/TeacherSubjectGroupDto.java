package com.andrey.rating_system_project.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherSubjectGroupDto {
    private Integer groupId;
    private String groupName;
    private Integer subjectId;
    private String subjectName;
}