package com.portal.teachercontentportal.repository;

import com.portal.teachercontentportal.model.User;
import com.portal.teachercontentportal.model.Assignment;
import com.portal.teachercontentportal.model.AssignmentSubmission;
import com.portal.teachercontentportal.service.HashService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long>{
    boolean existsByAssignmentAndStudent(Assignment assignment, User student);
    List<AssignmentSubmission> findByAssignment(Assignment assignment);
    Optional<AssignmentSubmission> findByAssignmentAndStudent(Assignment assignment, User student);
    boolean existsByhashValue(String hash);

}
