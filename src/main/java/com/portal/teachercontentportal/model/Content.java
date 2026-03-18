package com.portal.teachercontentportal.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name="content")
@Getter
@Setter
public class Content {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String fileUrl;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name="uploaded_by", nullable = false)
    private User uploadedBy;

    public Content(){}

    public Content(String title, String fileUrl, LocalDateTime createdAt, User uploadedBy)
    {
        this.title=title;
        this.fileUrl=fileUrl;
        this.createdAt=createdAt;
        this.uploadedBy=uploadedBy;
    }

}
