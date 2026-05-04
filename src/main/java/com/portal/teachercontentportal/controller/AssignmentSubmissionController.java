package com.portal.teachercontentportal.controller;


import com.portal.teachercontentportal.dto.SubmissionResponse;
import com.portal.teachercontentportal.service.AssignmentSubmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class AssignmentSubmissionController {
    private final AssignmentSubmissionService submissionService;

    public AssignmentSubmissionController(AssignmentSubmissionService submissionService)
    {
        this.submissionService=submissionService;
    }

    @PostMapping({"/student/assignment/{assignmentId}/submit", "/student/assignments/{assignmentId}/submit"})
    public ResponseEntity<SubmissionResponse> submitAssignment(@PathVariable Long assignmentId, @RequestParam MultipartFile file, Authentication authentication)
    {
        String studentUserId=authentication.getName();

        SubmissionResponse response=submissionService.submitAssignment(assignmentId, file, studentUserId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping({"/folders/assignment/{assignmentId}/submissions", "/folder/assignments/{assignmentId}/submissions"})
    public ResponseEntity<List<SubmissionResponse>> getSubmissions(@PathVariable Long assignmentId, Authentication authentication)
    {
        String teacherUserId=authentication.getName();
        List<SubmissionResponse> submissions=submissionService.getSubmissionForAssignment(assignmentId, teacherUserId);

        return ResponseEntity.ok(submissions);
    }
}
