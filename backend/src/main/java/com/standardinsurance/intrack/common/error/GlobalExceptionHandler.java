package com.standardinsurance.intrack.common.error;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Single translation point from exceptions to the {@link ErrorResponse} envelope (CLAUDE.md §9).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex, HttpServletRequest request) {
        ErrorCode code = ex.errorCode();
        return build(code.status(), code.name(), ex.getMessage(), List.of(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                           HttpServletRequest request) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(GlobalExceptionHandler::formatFieldError)
                .toList();
        return build(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION.name(), "Validation failed", details, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex,
                                                            HttpServletRequest request) {
        // Method-security (@PreAuthorize) denials surface here; map to 403 (not the 500 catch-all).
        return build(HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN.name(), "Access denied", List.of(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL.name(),
                "An unexpected error occurred", List.of(), request);
    }

    private static String formatFieldError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }

    private static ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String message,
                                                       List<String> details, HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.of(status.value(), code, message, details, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
