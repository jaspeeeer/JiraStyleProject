package com.standardinsurance.intrack.auth;

import com.standardinsurance.intrack.auth.jwt.JwtAuthenticationFilter;
import com.standardinsurance.intrack.config.JwtProperties;
import java.time.Duration;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * Builds the httpOnly auth cookies (and their cleared variants for logout) from configured
 * TTLs and cookie flags.
 */
@Component
public class AuthCookieFactory {

    private final JwtProperties properties;

    public AuthCookieFactory(JwtProperties properties) {
        this.properties = properties;
    }

    public ResponseCookie accessCookie(String token) {
        return build(JwtAuthenticationFilter.ACCESS_COOKIE, token, properties.accessTtl());
    }

    public ResponseCookie refreshCookie(String token) {
        return build(JwtAuthenticationFilter.REFRESH_COOKIE, token, properties.refreshTtl());
    }

    public ResponseCookie clearAccessCookie() {
        return build(JwtAuthenticationFilter.ACCESS_COOKIE, "", Duration.ZERO);
    }

    public ResponseCookie clearRefreshCookie() {
        return build(JwtAuthenticationFilter.REFRESH_COOKIE, "", Duration.ZERO);
    }

    private ResponseCookie build(String name, String value, Duration maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(properties.cookieSecure())
                .sameSite(properties.cookieSameSite())
                .path("/")
                .maxAge(maxAge)
                .build();
    }
}
