package com.standardinsurance.intrack.auth.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import com.standardinsurance.intrack.config.JwtProperties;
import com.standardinsurance.intrack.user.Role;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    private final JwtTokenProvider provider = new JwtTokenProvider(new JwtProperties(
            "unit-test-secret-that-is-definitely-long-enough-256-bits-1234567890",
            Duration.ofMinutes(15),
            Duration.ofDays(7),
            false,
            "Lax"));

    @Test
    void accessTokenCarriesSubjectAndRole() {
        String token = provider.generateAccessToken("ada@intrack.local", Role.ADMIN);

        assertThat(provider.isValid(token, JwtTokenProvider.TYPE_ACCESS)).isTrue();
        assertThat(provider.isValid(token, JwtTokenProvider.TYPE_REFRESH)).isFalse();
        assertThat(provider.getSubject(token)).isEqualTo("ada@intrack.local");
        assertThat(provider.getRole(token)).isEqualTo("ADMIN");
    }

    @Test
    void refreshTokenIsTypedRefresh() {
        String token = provider.generateRefreshToken("ada@intrack.local");

        assertThat(provider.isValid(token, JwtTokenProvider.TYPE_REFRESH)).isTrue();
        assertThat(provider.isValid(token, JwtTokenProvider.TYPE_ACCESS)).isFalse();
    }

    @Test
    void garbageTokenIsInvalid() {
        assertThat(provider.isValid("not-a-jwt", JwtTokenProvider.TYPE_ACCESS)).isFalse();
    }
}
