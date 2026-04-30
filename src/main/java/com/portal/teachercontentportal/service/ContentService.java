package com.portal.teachercontentportal.service;

import com.portal.teachercontentportal.model.Content;
import com.portal.teachercontentportal.model.User;
import com.portal.teachercontentportal.model.Folder;
import com.portal.teachercontentportal.repository.FolderRepository;
import com.portal.teachercontentportal.repository.ContentRepository;
import com.portal.teachercontentportal.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ContentService {
    private final ContentRepository contentRepository;
    private final UserRepository userRepository;
    private final FolderRepository folderRepository;
    public ContentService(ContentRepository contentRepository, UserRepository userRepository, FolderRepository folderRepository)
    {
        this.contentRepository=contentRepository;
        this.userRepository=userRepository;
        this.folderRepository=folderRepository;
    }

    public Content uploadContent(String title, String fileUrl, String userId, Long folderId,String hash)
    {
        User user=userRepository.findByUserId(userId)
                .orElseThrow(()->new RuntimeException("User not found"));

        Folder folder=folderRepository.findById(folderId)
                .orElseThrow(()->new RuntimeException("Folder not found"));

        if(!folder.getTeacher().getId().equals(user.getId()))
        {
            throw new RuntimeException("Unauthorized");
        }

        if(contentRepository.existsByHashVal(hash))
        {
            throw new RuntimeException("Plagiarism detected");
        }
        Content content=new Content();
        content.setTitle(title);
        content.setFileUrl(fileUrl);
        content.setCreatedAt(LocalDateTime.now());
        content.setUploadedBy(user);
        content.setFolder(folder);
        content.setHashVal(hash);


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
        contentRepository.delete(content);
    }

    @Transactional
    public void deleteContentByFolder(Long folderId)
    {
        contentRepository.deleteByFolder_Id(folderId);
    }
}
