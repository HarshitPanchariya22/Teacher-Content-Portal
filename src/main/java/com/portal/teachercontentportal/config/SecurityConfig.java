package com.portal.teachercontentportal.config;
import com.portal.teachercontentportal.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
                .csrf(AbstractHttpConfigurer::disable) .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))// cross site request forging -> any website should not send request to.
                //disabled because only happens when user and server creates a session which is not the case for jwt
                .authorizeHttpRequests(auth -> auth

                                .requestMatchers("/login").permitAll()
                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                .requestMatchers("/teacher/**").hasRole("TEACHER")
                                .requestMatchers("/student/**").hasRole("STUDENT")

                        .anyRequest().authenticated()
                ) // every other request MUST be authenticated like for /courses /users

                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
                // since JWTfilter also checks the request it is now added before the checks mentioned on above line
        //request -> Our JwtFilter -> UsernamePasswordAuthenticationFIlter -> Controller
        return http.build();

    }
}
