package com.portal.teachercontentportal.security;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    private static final String SECRET = System.getenv("JWT_SECRET_STRING");

    public String generateToken(String userId) {

        Algorithm algorithm = Algorithm.HMAC256(SECRET);
        final long Expiration = 3600000;

        return JWT.create()
                .withSubject(userId)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + Expiration))
                .sign(algorithm);
    }
    public DecodedJWT validateToken(String token) {

        Algorithm algorithm = Algorithm.HMAC256(SECRET);

        return JWT.require(algorithm)
                .build()
                .verify(token);
    }

}
