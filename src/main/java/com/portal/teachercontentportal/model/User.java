package com.portal.teachercontentportal.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@NoArgsConstructor
@Setter
@Getter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
//    private String name;
//    @Column(nullable = false,unique = true) // this value cannot be null and should be unique
    private String userId;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING) // added string enum type to role
    private Role role;


    public User(String userId, String password, Role role) {
     this.userId=userId;
     this.password = password;
     this.role = role;
    }
}
