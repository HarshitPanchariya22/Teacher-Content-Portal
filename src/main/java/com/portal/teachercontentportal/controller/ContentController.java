package com.portal.teachercontentportal.controller;

import com.portal.teachercontentportal.dto.ContentResponse;
import com.portal.teachercontentportal.model.Content;
import com.portal.teachercontentportal.service.ContentService;
import com.portal.teachercontentportal.service.Extraction;
import com.portal.teachercontentportal.service.HashService;
import com.portal.teachercontentportal.service.S3Service;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/content")
public class ContentController {

    private final ContentService contentService;
    private final S3Service s3Service;
    private final Extraction extract;
    private final HashService hashService;
    public ContentController(ContentService contentService, S3Service s3Service,Extraction extraction, HashService hashService)
    {
        this.contentService = contentService;
        this.s3Service = s3Service;
        this.extract = extraction;
        this.hashService = hashService;
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<Content> uploadContent(
            @RequestParam String title,
            @RequestParam("file") MultipartFile file,
            @RequestParam Long folderId
    )
    {

           Authentication auth= SecurityContextHolder.getContext().getAuthentication();
        String fileUrl = s3Service.fileUpload(file);

        String userId = auth.getPrincipal().toString();

        Content content = contentService.uploadContent(title, fileUrl, userId, folderId);
        return ResponseEntity.ok(content);
    }

    @GetMapping
    public ResponseEntity<List<Content>> getAllContent()
    {
        return ResponseEntity.ok(contentService.getAllContent());
    }

    @GetMapping("/my")
    public ResponseEntity<List<ContentResponse>> getMyContent() {
        Authentication auth= SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getPrincipal().toString();
        List<Content> contents = contentService.getContentByUser(userId);

        List<ContentResponse> response = contents.stream().map(c -> {
            return new ContentResponse(
                    c.getId(),
                    c.getTitle(), s3Service.generatePresignedUrl(c.getFileUrl())
            );
        }).toList();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteContent(@PathVariable Long id)
    {
        Authentication auth=SecurityContextHolder.getContext().getAuthentication();
        String userId=auth.getName();
        contentService.deleteContent(id, userId);
        return ResponseEntity.ok("Content deleted successfully");
    }
}