package com.portal.teachercontentportal.controller;

import com.portal.teachercontentportal.model.Assignment;
import com.portal.teachercontentportal.model.AssignmentSubmission;
import com.portal.teachercontentportal.service.AssignmentSubmissionService;
import com.portal.teachercontentportal.service.S3Service;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.portal.teachercontentportal.dto.SubmissionResponse;

import java.util.List;

@RestController
public class AssignmentSubmissionController {
    private final AssignmentSubmissionService submissionService;
    private final S3Service s3Service;

    public AssignmentSubmissionController(AssignmentSubmissionService submissionService, S3Service s3Service)
    {
        this.submissionService=submissionService;
        this.s3Service=s3Service;
    }

    @PostMapping("/student/assignments/{assignmentId}/submit")
    public ResponseEntity<SubmissionResponse> submitAssignment(@PathVariable Long assignmentId, @RequestParam MultipartFile file, Authentication authentication)
    {
        String studentUserId=authentication.getName();

        AssignmentSubmission submission=submissionService.submitAssignment(assignmentId, file, studentUserId);

        SubmissionResponse response=toSubmissionResponse(submission);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/teacher/assignments/{assignmentId}/submissions")
    public ResponseEntity<List<SubmissionResponse>> getSubmissions(@PathVariable Long assignmentId, Authentication authentication)
    {
        String teacherUserId=authentication.getName();
        List<AssignmentSubmission> submissions=submissionService.getSubmissionForAssignment(assignmentId, teacherUserId);

        List<SubmissionResponse> response=submissions.stream().map(this::toSubmissionResponse).toList();
        return ResponseEntity.ok(response);
    }

    private SubmissionResponse toSubmissionResponse(AssignmentSubmission submission)
    {
        return new SubmissionResponse(submission.getId(), submission.getStudent().getUserId(), s3Service.generatePresignedUrl(submission.getFileUrl()), submission.getSubmittedAt());
    }
}
