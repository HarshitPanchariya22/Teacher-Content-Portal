package com.portal.teachercontentportal.controller;

import com.portal.teachercontentportal.dto.AssignmentRequest;
import com.portal.teachercontentportal.model.Assignment;
import com.portal.teachercontentportal.service.AssignmentService;
import org.hibernate.dialect.function.array.AbstractArrayTrimFunction;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teacher/assignments")
public class AssignmentController {
    private final AssignmentService assignmentService;
    public AssignmentController(AssignmentService assignmentService)
    {
        this.assignmentService=assignmentService;
    }

    @PostMapping
    public ResponseEntity<Assignment> createAssignment(@RequestBody AssignmentRequest request, Authentication authentication)
    {
        String teacherUserId=authentication.getName();
        Assignment assignment= assignmentService.createAssignment(
                request.getTitle(),
                request.getDescription(),
                request.getFolderId(),
                teacherUserId,
                request.getDueDate()
        );
        return ResponseEntity.ok(assignment);
    }

    @GetMapping
    public ResponseEntity<List<Assignment>> getMyAssignments(Authentication authentication)
    {
        String teacherUserId=authentication.getName();
        return ResponseEntity.ok(assignmentService.getAssignmentByTeacher(teacherUserId));
    }

    @GetMapping("/folder/{folderId}")
    public ResponseEntity<List<Assignment>> getAssignmentsByFolder(@PathVariable Long folderId, Authentication authentication)
    {
        String teacherUserId=authentication.getName();
        return ResponseEntity.ok(assignmentService.getAssignmentsByFolder(folderId, teacherUserId));
    }

    @GetMapping("/{assignmentId}")
    public ResponseEntity<Assignment> getAssignmentById(@PathVariable Long assignmentId, Authentication authentication)
    {
        String teacherUserId=authentication.getName();
        return ResponseEntity.ok(assignmentService.getAssignmentById(assignmentId, teacherUserId));
    }

    @PutMapping("/{assignmentId}/toggle")
    public ResponseEntity<Assignment> toggleAssignmentStatus(@PathVariable Long assignmentId, Authentication authentication)
    {
        String teacherUserId=authentication.getName();
        return ResponseEntity.ok(assignmentService.toggleAssignmentStatus(assignmentId, teacherUserId));
    }

}
