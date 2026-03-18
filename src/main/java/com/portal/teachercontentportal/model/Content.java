package com.portal.teachercontentportal.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Content {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String title;
    private String subject;

    @ManyToOne
    private User uploadedBy;

    private LocalDateTime createdAt;
}
