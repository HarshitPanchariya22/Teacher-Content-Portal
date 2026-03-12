package com.portal.teachercontentportal.dto;
import com.portal.teachercontentportal.model.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class RegisterRequest {
    private String userId;
    private String password;
    private Role role;
}
