package com.standardinsurance.intrack.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT / auth-cookie settings, bound from {@code intrack.jwt.*}.
 */
@ConfigurationProperties(prefix = "intrack.jwt")
public record JwtProperties(
        String secret,
        Duration accessTtl,
        Duration refreshTtl,
        boolean cookieSecure,
        String cookieSameSite
) {
}
