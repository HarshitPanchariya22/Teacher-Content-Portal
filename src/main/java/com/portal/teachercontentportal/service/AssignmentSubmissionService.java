package com.portal.teachercontentportal.service;

import com.portal.teachercontentportal.model.User;
import com.portal.teachercontentportal.model.Folder;
import com.portal.teachercontentportal.model.Assignment;
import com.portal.teachercontentportal.model.AssignmentSubmission;
import com.portal.teachercontentportal.repository.AssignmentRepository;
import com.portal.teachercontentportal.repository.AssignmentSubmissionRepository;
import com.portal.teachercontentportal.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AssignmentSubmissionService {
    private final AssignmentSubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    public AssignmentSubmissionService(AssignmentSubmissionRepository submissionRepository, AssignmentRepository assignmentRepository, UserRepository userRepository, S3Service s3Service)
    {
        this.submissionRepository=submissionRepository;
        this.assignmentRepository=assignmentRepository;
        this.userRepository=userRepository;
        this.s3Service=s3Service;
    }

    public AssignmentSubmission submitAssignment(Long assignmentId, MultipartFile file, String studentUserId)
    {
        User student=userRepository.findByUserId(studentUserId)
                .orElseThrow(()->new RuntimeException("Student not found"));
        Assignment assignment=assignmentRepository.findById(assignmentId)
                .orElseThrow(()->new RuntimeException("Assignment not found"));
        if(!assignment.isOpen())
        {
            throw new RuntimeException("Assignment is closed");
        }
        Folder folder=assignment.getFolder();
        if(!folder.getYear().equals(student.getYear()) || !folder.getBranch().equals(student.getBranch()))
        {
            throw new RuntimeException("Not allowed!");
        }
        boolean alreadySubmitted=submissionRepository.existsByAssignmentAndStudent(assignment, student);
        if(alreadySubmitted)
        {
            throw new RuntimeException("Assignment already submitted!");
        }
        String fileUrl= s3Service.fileUpload(file);
        AssignmentSubmission submission=new AssignmentSubmission();
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setFileUrl(fileUrl);
        submission.setSubmittedAt(LocalDateTime.now());
        return submissionRepository.save(submission);
    }
    public List<AssignmentSubmission> getSubmissionForAssignment(Long assignmentId, String teacherUserId)
    {
        User teacher=userRepository.findByUserId(teacherUserId)
                .orElseThrow(()->new RuntimeException("Teacher not found"));
        Assignment assignment=assignmentRepository.findById(assignmentId)
                .orElseThrow(()->new RuntimeException("Assignment not found"));
        if(!assignment.getCreatedBy().getId().equals(teacher.getId()))
        {
            throw new RuntimeException("Unauthorized");
        }
        return submissionRepository.findByAssignment(assignment);
    }
}
