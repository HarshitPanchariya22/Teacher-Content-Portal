package com.portal.teachercontentportal.service;

import com.portal.teachercontentportal.model.Assignment;
import com.portal.teachercontentportal.model.User;
import com.portal.teachercontentportal.model.Folder;
import com.portal.teachercontentportal.repository.AssignmentRepository;
import com.portal.teachercontentportal.repository.FolderRepository;
import com.portal.teachercontentportal.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final FolderRepository folderRepository;
    public AssignmentService(AssignmentRepository assignmentRepository, UserRepository userRepository, FolderRepository folderRepository)
    {
        this.assignmentRepository=assignmentRepository;
        this.userRepository=userRepository;
        this.folderRepository=folderRepository;
    }

    public Assignment createAssignment(String title, String description, Long folderId, String teacherUserId, LocalDateTime dueDate)
    {
        User teacher=userRepository.findByUserId(teacherUserId)
                .orElseThrow(()-> new RuntimeException("Teacher not found"));

        Folder folder=folderRepository.findById(folderId)
                .orElseThrow(()->new RuntimeException("Folder not found"));

        if(!folder.getTeacher().getId().equals(teacher.getId()))
        {
            throw new RuntimeException("Unauthorized");
        }
        Assignment assignment=new Assignment();
        assignment.setTitle(title);
        assignment.setDescription(description);
        assignment.setFolder(folder);
        assignment.setCreatedBy(teacher);
        assignment.setCreatedAt(LocalDateTime.now());
        assignment.setOpen(true);
        assignment.setDueDate(dueDate);
        return assignmentRepository.save(assignment);
    }

    public List<Assignment> getAssignmentsByFolder(Long folderId, String teacherUserId)
    {
        User teacher=userRepository.findByUserId(teacherUserId)
                .orElseThrow(()-> new RuntimeException("Teacher not found"));

        Folder folder=folderRepository.findById(folderId)
                .orElseThrow(()->new RuntimeException("Folder not found"));

        if(!folder.getTeacher().getId().equals(teacher.getId()))
        {
            throw new RuntimeException("Unauthorized");
        }
        return assignmentRepository.findByFolder(folder);
    }

    public Assignment getAssignmentById(Long assignmentId, String teacherUserId)
    {
        User teacher = userRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        if (!assignment.getCreatedBy().getId().equals(teacher.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        return assignment;
    }

    public List<Assignment> getAssignmentByTeacher(String teacherUserId)
    {
        User teacher=userRepository.findByUserId(teacherUserId)
                .orElseThrow(()-> new RuntimeException("Teacher not found"));
        return assignmentRepository.findByCreatedBy(teacher);
    }

    public Assignment toggleAssignmentStatus(Long assingmentId, String teacherUserId)
    {
        User teacher=userRepository.findByUserId(teacherUserId)
                .orElseThrow(()-> new RuntimeException("Teacher not found"));
        Assignment assignment=assignmentRepository.findById(assingmentId)
                .orElseThrow(()->new RuntimeException("Assignment not found"));
        if(!assignment.getCreatedBy().getId().equals(teacher.getId()))
        {
            throw new RuntimeException("Unauthorized");
        }
        assignment.setOpen(!assignment.isOpen());
        return assignmentRepository.save(assignment);
    }
}
