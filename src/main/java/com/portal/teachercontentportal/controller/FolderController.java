package com.portal.teachercontentportal.controller;

import com.portal.teachercontentportal.dto.ContentResponse;
import com.portal.teachercontentportal.model.Content;
import com.portal.teachercontentportal.model.Folder;
import com.portal.teachercontentportal.model.User;
import com.portal.teachercontentportal.repository.ContentRepository;
import com.portal.teachercontentportal.repository.FolderRepository;
import com.portal.teachercontentportal.repository.UserRepository;
import com.portal.teachercontentportal.service.ContentService;

import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;import com.portal.teachercontentportal.service.S3Service;


@RestController
@RequestMapping("/folders")
public class FolderController{
    private final FolderRepository folderRepository;
    private final UserRepository userRepository;
    private final ContentRepository contentRepository;
    private final S3Service s3Service;
    private final ContentService contentService;
    public FolderController(FolderRepository folderRepository, UserRepository userRepository,ContentRepository contentRepository, ContentService contentService,S3Service s3Service)
    {
        this.contentRepository = contentRepository;
        this.folderRepository=folderRepository;
        this.userRepository=userRepository;
        this.s3Service =  s3Service;
        this.contentService=contentService;
    }

    @PostMapping("/upload")
    public Folder createFolder(@RequestBody Folder folder, Principal principal)
    {
        User teacher=userRepository.findByUserId(principal.getName())
                .orElseThrow(()-> new RuntimeException("User not found"));

        folder.setTeacher(teacher);

        return folderRepository.save(folder);
    }

    @GetMapping
    public List<Folder> getMyFolders(Principal principal)
    {
        User teacher=userRepository.findByUserId(principal.getName())
                .orElseThrow(()-> new RuntimeException("User not found"));
        return folderRepository.findByTeacher(teacher);
    }

    @GetMapping("/files/{folderId}")
    public List<ContentResponse> getFilesInsideFolder(@PathVariable Long folderId,
                                              Principal principal) {

        User teacher = userRepository.findByUserId(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        if (!folder.getTeacher().getId().equals(teacher.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        List<Content> files = contentRepository.findByFolder(folder);

        return files.stream()
                .map(file -> new ContentResponse(
                        file.getId(),
                        file.getTitle(),
                        s3Service.generatePresignedUrl(file.getFileUrl())
                ))
                .toList();
    }

    @DeleteMapping("/{folderId}")
    public String deleteFolder(@PathVariable Long folderId, Principal principal)
    {
        User teacher=userRepository.findByUserId(principal.getName())
                .orElseThrow(()->new RuntimeException("User not found"));
        Folder folder=folderRepository.findByIdAndTeacher(folderId, teacher)
                .orElseThrow(()->new RuntimeException("Folder not found or unauthorized"));
        contentService.deleteContentByFolder(folderId);
        folderRepository.delete(folder);
        return "folder deleted successfully";
    }


   @PutMapping("/toggle/{folderId}")
   public String put(@PathVariable Long folderId, Principal principal) {
        User teacher = userRepository.findByUserId(principal.getName()).orElseThrow(()->new RuntimeException("Cannot find user"));
        Folder folder = folderRepository.findById(folderId).orElseThrow(()->new RuntimeException("Cannot find the folder"));

        folder.setEnabled(!folder.isEnabled());
        folderRepository.save(folder);
        return  folder.isEnabled()?"Enabled":"Disabled";
   }
}
