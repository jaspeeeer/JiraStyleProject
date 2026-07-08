package com.standardinsurance.intrack.common.error;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Fast unit test (no Spring context, no Docker) — runs in the surefire {@code test} phase.
 * Verifies the error envelope's defaulting behavior.
 */
class ErrorResponseTest {

    @Test
    void nullDetailsBecomeEmptyList() {
        ErrorResponse response = ErrorResponse.of(404, "NOT_FOUND", "missing", null, "/api/v1/x");

        assertThat(response.details()).isEmpty();
        assertThat(response.status()).isEqualTo(404);
        assertThat(response.code()).isEqualTo("NOT_FOUND");
        assertThat(response.timestamp()).isNotNull();
    }
}
