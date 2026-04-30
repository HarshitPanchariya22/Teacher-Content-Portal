package com.portal.teachercontentportal.repository;

import com.portal.teachercontentportal.model.Content;
import com.portal.teachercontentportal.model.Folder;
import com.portal.teachercontentportal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContentRepository extends JpaRepository<Content, Long>{
    List<Content> findByUploadedBy(User user);
    List<Content> findAllByOrderByCreatedAtDesc();
    List<Content> findByFolder(Folder folder);
    void deleteByFolder_Id(Long folderId);
}
