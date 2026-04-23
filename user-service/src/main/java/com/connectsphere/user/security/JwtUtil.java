package com.connectsphere.user.security;

import com.connectsphere.user.config.JwtProperties;
import com.connectsphere.user.domain.entity.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String email) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(jwtProperties.issuer())
                .subject(email)
                .claim("email", email)
                .claim("role", "USER")
                .claim("scope", "user")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(jwtProperties.expiration())))
                .signWith(secretKey)
                .compact();
    }

    public String generateToken(UserAccount userAccount) {
        Instant now = Instant.now();
        String role = userAccount.getRole() == null || userAccount.getRole().isBlank() ? "USER" : userAccount.getRole();
        return Jwts.builder()
                .issuer(jwtProperties.issuer())
                .subject(userAccount.getId().toString())
                .claim("email", userAccount.getEmail())
                .claim("fullName", userAccount.getDisplayName())
                .claim("displayName", userAccount.getDisplayName())
                .claim("role", role)
                .claim("scope", role.toLowerCase())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(jwtProperties.expiration())))
                .signWith(secretKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    public String extractEmail(String token) {
        Claims claims = parseClaims(token);
        String email = claims.get("email", String.class);
        return email == null ? claims.getSubject() : email;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(jwtProperties.issuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
