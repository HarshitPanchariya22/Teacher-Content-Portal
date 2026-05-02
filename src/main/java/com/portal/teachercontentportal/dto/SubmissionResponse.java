package com.portal.teachercontentportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SubmissionResponse {
    private Long submissionId;
    private String studentUserId;
    private String fileUrl;
    private LocalDateTime submittedAt;
    private Double similarityScore;
    private String matchedStudentID;
}
