package com.standardinsurance.intrack.auth;

import com.standardinsurance.intrack.auth.dto.ForgotPasswordRequestDto;
import com.standardinsurance.intrack.auth.dto.LoginRequestDto;
import com.standardinsurance.intrack.auth.dto.RegisterRequestDto;
import com.standardinsurance.intrack.auth.dto.ResetPasswordRequestDto;
import com.standardinsurance.intrack.auth.jwt.JwtAuthenticationFilter;
import com.standardinsurance.intrack.common.error.ApiException;
import com.standardinsurance.intrack.common.error.ErrorCode;
import com.standardinsurance.intrack.user.dto.UserResponseDto;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthCookieFactory cookieFactory;

    public AuthController(AuthService authService, AuthCookieFactory cookieFactory) {
        this.authService = authService;
        this.cookieFactory = cookieFactory;
    }

    @PostMapping("/register")
    public UserResponseDto register(@Valid @RequestBody RegisterRequestDto request,
                                    HttpServletResponse response) {
        AuthResult result = authService.register(request);
        setSessionCookies(response, result);
        return result.user();
    }

    @PostMapping("/login")
    public UserResponseDto login(@Valid @RequestBody LoginRequestDto request,
                                 HttpServletResponse response) {
        AuthResult result = authService.login(request);
        setSessionCookies(response, result);
        return result.user();
    }

    @PostMapping("/refresh")
    public UserResponseDto refresh(
            @CookieValue(name = JwtAuthenticationFilter.REFRESH_COOKIE, required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken == null) {
            throw new ApiException(ErrorCode.INVALID_TOKEN, "Missing refresh token");
        }
        AuthResult result = authService.refresh(refreshToken);
        setSessionCookies(response, result);
        return result.user();
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletResponse response) {
        addCookie(response, cookieFactory.clearAccessCookie());
        addCookie(response, cookieFactory.clearRefreshCookie());
    }

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto request) {
        authService.forgotPassword(request);
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequestDto request) {
        authService.resetPassword(request);
    }

    @GetMapping("/me")
    public UserResponseDto me(Authentication authentication) {
        return authService.me(authentication.getName());
    }

    /** Stubbed SSO entrypoint — real IdP integration is deferred. */
    @GetMapping("/sso")
    public Map<String, String> sso() {
        return Map.of("status", "NOT_CONFIGURED", "message", "SSO is not configured yet");
    }

    private void setSessionCookies(HttpServletResponse response, AuthResult result) {
        addCookie(response, cookieFactory.accessCookie(result.accessToken()));
        addCookie(response, cookieFactory.refreshCookie(result.refreshToken()));
    }

    private void addCookie(HttpServletResponse response, ResponseCookie cookie) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
