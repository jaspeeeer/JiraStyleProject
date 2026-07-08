package com.standardinsurance.intrack.common.error;

import org.springframework.http.HttpStatus;

/**
 * Catalog of domain error codes. Each maps to a stable machine-readable {@code code}
 * (returned in {@link ErrorResponse}) and a default HTTP status. New features add their
 * codes here so the catalog stays centralized.
 */
public enum ErrorCode {

    VALIDATION(HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED),
    FORBIDDEN(HttpStatus.FORBIDDEN),
    NOT_FOUND(HttpStatus.NOT_FOUND),
    CONFLICT(HttpStatus.CONFLICT),
    INTERNAL(HttpStatus.INTERNAL_SERVER_ERROR),

    // Auth & users
    BAD_CREDENTIALS(HttpStatus.UNAUTHORIZED),
    EMAIL_TAKEN(HttpStatus.CONFLICT),
    INVALID_TOKEN(HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND);

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
