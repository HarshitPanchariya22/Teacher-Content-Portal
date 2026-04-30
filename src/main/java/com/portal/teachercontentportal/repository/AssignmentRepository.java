package com.portal.teachercontentportal.repository;

import com.portal.teachercontentportal.model.Assignment;
import com.portal.teachercontentportal.model.Folder;
import com.portal.teachercontentportal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long>{
    List<Assignment> findByFolder(Folder folder);
    List<Assignment> findByCreatedBy(User createdBy);
}
