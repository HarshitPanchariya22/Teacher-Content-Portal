package com.portal.teachercontentportal.service;

import com.portal.teachercontentportal.dto.SubmissionResponse;
import com.portal.teachercontentportal.model.User;
import com.portal.teachercontentportal.model.Folder;
import com.portal.teachercontentportal.model.Assignment;
import com.portal.teachercontentportal.model.AssignmentSubmission;
import com.portal.teachercontentportal.repository.AssignmentRepository;
import com.portal.teachercontentportal.repository.AssignmentSubmissionRepository;
import com.portal.teachercontentportal.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
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

    public AssignmentSubmissionService(
            AssignmentSubmissionRepository submissionRepository,
            AssignmentRepository assignmentRepository,
            UserRepository userRepository,
            S3Service s3Service,
            Extraction extraction,
            HashService hashService,
            TfidfService tfidfService
    ) {
        this.submissionRepository = submissionRepository;
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.s3Service = s3Service;
        this.extraction = extraction;
        this.hashService = hashService;
        this.tfidfService = tfidfService;
    }

    public SubmissionResponse submitAssignment(Long assignmentId, MultipartFile file, String studentUserId) {

        User student = userRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        if (!assignment.isOpen()) {
            throw new RuntimeException("Assignment is closed");
        }

        Folder folder = assignment.getFolder();
        if (!folder.getYear().equals(student.getYear()) ||
                !folder.getBranch().equals(student.getBranch())) {
            throw new RuntimeException("Not allowed!");
        }

        if (submissionRepository.existsByAssignmentAndStudent(assignment, student)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Assignment already submitted!");
        }

        String text = extraction.extractText(file);
        String hash = hashService.generateHash(text);

        List<AssignmentSubmission> existingSubmissions =
                submissionRepository.findByAssignment(assignment);

        List<String> corpus = new ArrayList<>();
        for (AssignmentSubmission s : existingSubmissions) {
            if (s.getExtractedText() != null) {
                corpus.add(s.getExtractedText());
            }
        }

        double maxSimilarity = 0.0;
        AssignmentSubmission bestMatch = null;

        for (AssignmentSubmission s : existingSubmissions) {
            if (s.getExtractedText() == null) continue;

            double similarity = tfidfService.cosineSimilarity(
                    text,
                    s.getExtractedText(),
                    corpus
            );

            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
                bestMatch = s;
            }
        }

        String fileUrl = s3Service.fileUpload(file);

        AssignmentSubmission submission = new AssignmentSubmission();
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setFileUrl(fileUrl);
        submission.setHash(hash);
        submission.setExtractedText(text);
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setSimilarityScore(maxSimilarity * 100); // percentage
        submission.setMatchedWith(bestMatch);

        AssignmentSubmission saved = submissionRepository.save(submission);

        SubmissionResponse response = new SubmissionResponse(
                saved.getId(),
                student.getUserId(),
                fileUrl,
                saved.getSubmittedAt(),
                saved.getSimilarityScore(),
                bestMatch != null ? bestMatch.getStudent().getUserId() : null,
                null
        );

        if (maxSimilarity > 0.8) {
            response.setWarning("High similarity detected with another submission");
        } else {
            response.setWarning(null);
        }

        return response;
    }

    public List<SubmissionResponse> getSubmissionForAssignment(Long assignmentId, String teacherUserId) {

        User teacher = userRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        if (!assignment.getCreatedBy().getId().equals(teacher.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        List<AssignmentSubmission> submissions =
                submissionRepository.findByAssignment(assignment);

        return submissions.stream().map(sub ->
                new SubmissionResponse(
                        sub.getId(),
                        sub.getStudent().getUserId(),
                        sub.getFileUrl(),
                        sub.getSubmittedAt(),
                        sub.getSimilarityScore(),
                        sub.getMatchedWith() != null
                                ? sub.getMatchedWith().getStudent().getUserId()
                                : null,
                        null
                )
        ).toList();
    }
}
