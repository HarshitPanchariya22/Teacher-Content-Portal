package com.portal.teachercontentportal.model;
import jakarta.persistence.*;
@Entity
@Table(name = "Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false,unique = true) // this value cannot be null and should be unique
    private String userId;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING) // added string enum type to role
    private Role role;


    public User(String name, String password,String userId,Role role)
    {
        this.name = name;
        this.password = password;
        this.userId = userId;
        this.role = role;
    }

    // Getter Setter func
    public Long getId()
    {
        return id;
    }
    public void setId(Long id)
    {
        this.id = id;
    }
    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public String getuserId()
    {
        return userId;
    }
    public void setuserId(String userId)
    {
        this.userId = userId;
    }
    public String getPassword()
    {
        return password;
    }
    public void setPassword(String password)
    {
        this.password = password;
    }
    public Role getRole()
    {
        return role;
    }
    public void setRole(Role role)
    {
        this.role = role;
    }
}
