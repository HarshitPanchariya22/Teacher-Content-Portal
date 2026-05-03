package com.portal.teachercontentportal.service;

import com.portal.teachercontentportal.dto.SubmissionResponse;
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
import java.util.ArrayList;
import java.util.List;

@Service
public class AssignmentSubmissionService {
    private final AssignmentSubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final Extraction extraction;
    private final HashService hashService;
    private final TfidfService tfidfService;

    public AssignmentSubmissionService(AssignmentSubmissionRepository submissionRepository,
                                       AssignmentRepository assignmentRepository,
                                       UserRepository userRepository, S3Service s3Service,
                                       Extraction extraction,
                                       HashService hashService,
                                       TfidfService tfidfService)
    {
        this.submissionRepository=submissionRepository;
        this.assignmentRepository=assignmentRepository;
        this.userRepository=userRepository;
        this.s3Service=s3Service;
        this.extraction = extraction;
        this.hashService = hashService;
        this.tfidfService = tfidfService;
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
        String text = extraction.extractText(file);
        String hash = hashService.generateHash(text);
        if(submissionRepository.existsByhashValue(hash))
        {
            throw new RuntimeException("Duplicate Submission detected");
        }
        List<AssignmentSubmission>existingSubmission = submissionRepository.findByAssignment(assignment);
        List<String>corpus = new ArrayList<>();
        for(AssignmentSubmission s  : existingSubmission)
        {
            if(s.getExtractedText() != null)
            {
                corpus.add(s.getExtractedText());
            }
        }
        double maxSimilarity = 0.0;
        AssignmentSubmission bestMatch =null;
        for(AssignmentSubmission s: existingSubmission)
        {
            if(s.getExtractedText() != null)continue;

            double similarity = tfidfService.cosineSimilarity(text,s.getExtractedText(),corpus);
            if(similarity>maxSimilarity) {
                maxSimilarity = similarity;
                bestMatch = s;
            }
        }
        String fileUrl= s3Service.fileUpload(file);
        AssignmentSubmission submission=new AssignmentSubmission();
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setFileUrl(fileUrl);
        submission.setHash(hash);
        submission.setExtractedText(text);
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setSimilarityScore(maxSimilarity);
        submission.setMatchedWith(bestMatch);
        return submissionRepository.save(submission);
    }
    public List<SubmissionResponse> getSubmissionForAssignment(Long assignmentId, String teacherUserId)
    {
        User teacher=userRepository.findByUserId(teacherUserId)
                .orElseThrow(()->new RuntimeException("Teacher not found"));
        Assignment assignment=assignmentRepository.findById(assignmentId)
                .orElseThrow(()->new RuntimeException("Assignment not found"));
        if(!assignment.getCreatedBy().getId().equals(teacher.getId()))
        {
            throw new RuntimeException("Unauthorized");
        }
        List<AssignmentSubmission> submissions = submissionRepository.findByAssignment(assignment);
        return submissions.stream().map(sub -> {
            SubmissionResponse s = new SubmissionResponse(sub.getId(),
                    sub.getStudent().getUserId(),
                    sub.getFileUrl(),sub.getSubmittedAt(),
                    sub.getSimilarityScore()!=null?Math.round(sub.getSimilarityScore()*10):0.0,
                    sub.getMatchedWith()!= null?sub.getMatchedWith().getStudent().getUserId():null);
            return s;
        }).toList();
    }


}
