package com.portal.teachercontentportal.controller;

import com.portal.teachercontentportal.dto.*;
import com.portal.teachercontentportal.security.JwtUtil;
import com.portal.teachercontentportal.service.AuthService;
import org.springframework.http.ResponseEntity;
import com.portal.teachercontentportal.model.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest)
    {
        User user = authService.authenticateUser(
                loginRequest.getUserId(),
                loginRequest.getPassword()
        );
        String token = jwtUtil.generateToken(
                loginRequest.getUserId(),
                loginRequest.getPassword()
        );
        return ResponseEntity.ok(new AuthResponse(token));
    }

}
