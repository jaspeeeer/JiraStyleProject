package com.standardinsurance.intrack.auth.jwt;

import com.standardinsurance.intrack.config.JwtProperties;
import com.standardinsurance.intrack.user.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

/**
 * Issues and verifies signed JWTs (HS256). Access tokens carry the user's role; refresh tokens
 * are used only to mint new access tokens. Stateless — no server-side token store (rotation is a
 * documented future enhancement).
 */
@Component
public class JwtTokenProvider {

    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TYPE = "type";

    private final SecretKey key;
    private final Duration accessTtl;
    private final Duration refreshTtl;

    public JwtTokenProvider(JwtProperties properties) {
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
        this.accessTtl = properties.accessTtl();
        this.refreshTtl = properties.refreshTtl();
    }

    public String generateAccessToken(String email, Role role) {
        return build(email, accessTtl, TYPE_ACCESS, role.name());
    }

    public String generateRefreshToken(String email) {
        return build(email, refreshTtl, TYPE_REFRESH, null);
    }

    private String build(String subject, Duration ttl, String type, String role) {
        Instant now = Instant.now();
        var builder = Jwts.builder()
                .subject(subject)
                .claim(CLAIM_TYPE, type)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(key);
        if (role != null) {
            builder.claim(CLAIM_ROLE, role);
        }
        return builder.compact();
    }

    /** Parses and verifies the token, throwing {@link io.jsonwebtoken.JwtException} if invalid. */
    public Jws<Claims> parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
    }

    public boolean isValid(String token, String expectedType) {
        try {
            Claims claims = parse(token).getPayload();
            return expectedType.equals(claims.get(CLAIM_TYPE, String.class));
        } catch (RuntimeException ex) {
            return false;
        }
    }

    public String getSubject(String token) {
        return parse(token).getPayload().getSubject();
    }

    public String getRole(String token) {
        return parse(token).getPayload().get(CLAIM_ROLE, String.class);
    }
}
