package com.andrey.rating_system_project.dto;

import lombok.Data;
import java.util.List;

@Data
public class PdfReportRequest {
    private Integer subjectId;
    private List<Integer> groupIds;
}