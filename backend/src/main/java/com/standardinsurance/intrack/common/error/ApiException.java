package com.standardinsurance.intrack.common.error;

/**
 * Base exception for domain errors. Services throw this (or a subclass) with an
 * {@link ErrorCode}; {@link GlobalExceptionHandler} translates it into an {@link ErrorResponse}
 * with the code's HTTP status.
 */
public class ApiException extends RuntimeException {

    private final ErrorCode errorCode;

    public ApiException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }
}
