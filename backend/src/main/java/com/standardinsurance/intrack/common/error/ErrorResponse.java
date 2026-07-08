package com.standardinsurance.intrack.common.error;

import java.time.Instant;
import java.util.List;

/**
 * Standard error envelope returned for every non-2xx response. See CLAUDE.md §9.
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        List<String> details,
        String path
) {
    public static ErrorResponse of(int status, String code, String message, List<String> details, String path) {
        return new ErrorResponse(Instant.now(), status, code, message, details == null ? List.of() : details, path);
    }
}
