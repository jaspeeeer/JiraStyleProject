package com.standardinsurance.intrack.auth;

import com.standardinsurance.intrack.user.dto.UserResponseDto;

/**
 * Internal result of an authentication operation. The controller turns the tokens into cookies
 * and returns only {@code user} in the response body.
 */
public record AuthResult(String accessToken, String refreshToken, UserResponseDto user) {
}
