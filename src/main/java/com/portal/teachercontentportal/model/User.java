package com.portal.teachercontentportal.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@NoArgsConstructor
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "year")
    private Year year;

    @Enumerated(EnumType.STRING)
    private Branch branch;


    public User(String userId, String password, Role role) {
        this.userId = userId;
        this.password = password;
        this.role = role;
    }


    public User(String userId,
                String password,
                Role role,
                Year year,
                Branch branch) {

        this.userId = userId;
        this.password = password;
        this.role = role;
        this.year = year;
        this.branch = branch;
    }
}