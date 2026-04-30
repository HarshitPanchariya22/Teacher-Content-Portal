package com.portal.teachercontentportal.dto;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentRequest {
    private String title;
    private String description;
    private Long folderId;
    private LocalDateTime dueDate;
}
