package com.portal.teachercontentportal.service;

import com.portal.teachercontentportal.model.Content;
import com.portal.teachercontentportal.model.Assignment;
import com.portal.teachercontentportal.model.User;
import com.portal.teachercontentportal.model.Folder;
import com.portal.teachercontentportal.repository.AssignmentRepository;
import com.portal.teachercontentportal.repository.AssignmentSubmissionRepository;
import com.portal.teachercontentportal.repository.FolderRepository;
import com.portal.teachercontentportal.repository.ContentRepository;
import com.portal.teachercontentportal.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ContentService {
    private final ContentRepository contentRepository;
    private final UserRepository userRepository;
    private final FolderRepository folderRepository;
    private final AssignmentRepository assignmentRepository;
    private  final S3Service s3Service;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    public ContentService(ContentRepository contentRepository,
                          UserRepository userRepository,
                          FolderRepository folderRepository,
                          AssignmentRepository assignmentRepository, S3Service s3Service, AssignmentSubmissionRepository assignmentSubmissionRepository)
    {
        this.contentRepository=contentRepository;
        this.userRepository=userRepository;
        this.folderRepository=folderRepository;
        this.assignmentRepository = assignmentRepository;
        this.s3Service = s3Service;
        this.assignmentSubmissionRepository = assignmentSubmissionRepository;
    }

    public Content uploadContent(String title, String fileUrl, String userId, Long folderId)
    {
        User user=userRepository.findByUserId(userId)
                .orElseThrow(()->new RuntimeException("User not found"));

        Folder folder=folderRepository.findById(folderId)
                .orElseThrow(()->new RuntimeException("Folder not found"));

        if(!folder.getTeacher().getId().equals(user.getId()))
        {
            throw new RuntimeException("Unauthorized");
        }
        Content content=new Content();
        content.setTitle(title);
        content.setFileUrl(fileUrl);
        content.setCreatedAt(LocalDateTime.now());
        content.setUploadedBy(user);
        content.setFolder(folder);

        return contentRepository.save(content);
    }

    public List<Content> getAllContent() {
        return contentRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Content> getContentByUser(String userId) {

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return contentRepository.findByUploadedBy(user);
    }
    public void deleteContent(Long contentId, String userId)
    {
        Content content=contentRepository.findById(contentId)
                .orElseThrow(()-> new RuntimeException("Content not found"));
        if(!content.getUploadedBy().getUserId().equalsIgnoreCase(userId))
        {
            throw new RuntimeException("You are not allowed to delete this content");
        }
        s3Service.deleteFile(content.getFileUrl());
        contentRepository.delete(content);
    }

    @Transactional
    public void deleteContentByFolder(Long folderId)
    {
        contentRepository.deleteByFolder_Id(folderId);
    }

    @Transactional
    public void DeleteFolder(Long folderId, String userId)
    {
        Folder folder = folderRepository.findById(folderId).orElseThrow(()-> new RuntimeException("Folder not found"));
        if(!folder.getTeacher().getUserId().equalsIgnoreCase(userId))
        {
            throw new RuntimeException("Unauthorised to delete the folder");
        }
        List<Content>contents = contentRepository.findByFolder(folder);
        for(Content c :contents)
        {
            s3Service.deleteFile(c.getFileUrl());
        }
        List<Assignment> assignments = assignmentRepository.findByFolder(folder);
        for(Assignment a : assignments)
        {
            assignmentSubmissionRepository.deleteByAssignment(a);
        }

        assignmentRepository.deleteByFolderId(folderId);
        contentRepository.deleteByFolder_Id(folderId);
        folderRepository.delete(folder);
    }
}
