package com.portal.teachercontentportal.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final  JwtUtil jwtUtil;
    public JwtFilter(JwtUtil jwtUtil)
    {
        this.jwtUtil = jwtUtil;
    }
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {

         final String path = request.getServletPath();
        if(path.equals("/login")||
                path.startsWith("/pages/**") ||
                path.startsWith("/css/**") ||
                path.startsWith("/js/**")) {
            chain.doFilter(request, response);
            return;
        }
        String header = request.getHeader("Authorization");
        if(header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            DecodedJWT jwt = jwtUtil.validateToken(token);
            String userId = jwt.getSubject();
            System.out.println("Authenticated user: " + userId);
        }

        chain.doFilter(request, response);
    }
}