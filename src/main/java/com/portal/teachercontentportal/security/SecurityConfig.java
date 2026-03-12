package com.portal.teachercontentportal.security;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.net.http.HttpRequest;

@Configuration
public class SecurityConfig {
    private final JwtFilter jwtFilter;
    public SecurityConfig(JwtFilter jwtFilter)
    {
        this.jwtFilter = jwtFilter;
    }
    @Bean
    public PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws  Exception
    {
        http
                .csrf(AbstractHttpConfigurer::disable) // cross site request forging -> any website should not send request to.
                //disabled because only happens when user and server creates a session which is not the case for jwt
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers("/auth/**").permitAll().
                                // defines that anything with /auth/... is public and doesnt need authentication -> login or register
                                anyRequest().authenticated()) // every other request MUST be authenticated like for /courses /users
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
                // since JWTfilter also checks the request it is now added before the checks mentioned on above line
        //request -> Our JwtFilter -> UsernamePasswordAuthenticationFIlter -> Controller
        return http.build();

    }
}
