package com.portal.teachercontentportal.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name="assignments")
@Getter
@Setter
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000, nullable = false)
    private String description;

    @ManyToOne
    @JoinColumn(name="folder_id", nullable = false)
    private Folder folder;

    @ManyToOne
    @JoinColumn(name="created_by", nullable = false)
    private User createdBy;

    @Column(nullable = false)
    private boolean open;

    @Column(nullable = false)
    private LocalDateTime createdAt;
    private LocalDateTime dueDate;

}
