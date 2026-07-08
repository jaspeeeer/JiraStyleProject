package com.standardinsurance.intrack.auth;

import com.standardinsurance.intrack.common.email.EmailSender;
import com.standardinsurance.intrack.common.error.ApiException;
import com.standardinsurance.intrack.common.error.ErrorCode;
import com.standardinsurance.intrack.config.AppProperties;
import com.standardinsurance.intrack.user.UserEntity;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Issues, emails, and consumes single-use tokens shared by the password-reset and invite flows.
 */
@Service
public class PasswordResetService {

    private static final Duration TOKEN_TTL = Duration.ofHours(48);

    private final PasswordResetTokenRepository tokenRepository;
    private final EmailSender emailSender;
    private final AppProperties appProperties;

    public PasswordResetService(PasswordResetTokenRepository tokenRepository,
                                EmailSender emailSender,
                                AppProperties appProperties) {
        this.tokenRepository = tokenRepository;
        this.emailSender = emailSender;
        this.appProperties = appProperties;
    }

    /** Creates a fresh token for the user and returns its raw value. */
    public String issue(UserEntity user) {
        PasswordResetTokenEntity entity = new PasswordResetTokenEntity();
        entity.setUser(user);
        entity.setToken(UUID.randomUUID().toString());
        entity.setExpiresAt(Instant.now().plus(TOKEN_TTL));
        tokenRepository.save(entity);
        return entity.getToken();
    }

    public void sendPasswordReset(UserEntity user, String token) {
        String link = link("/reset-password", token);
        emailSender.send(user.getEmail(), "Reset your InTrack password",
                "Reset your password using this link:\n" + link);
    }

    public void sendInvite(UserEntity user, String token) {
        String link = link("/reset-password", token);
        emailSender.send(user.getEmail(), "You've been invited to InTrack",
                "Set your password and activate your account:\n" + link);
    }

    /** Validates a token and marks it used, returning it. Throws {@code INVALID_TOKEN} otherwise. */
    public PasswordResetTokenEntity consume(String token) {
        PasswordResetTokenEntity entity = tokenRepository.findByToken(token)
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_TOKEN, "Invalid or unknown token"));
        if (entity.getUsedAt() != null) {
            throw new ApiException(ErrorCode.INVALID_TOKEN, "Token already used");
        }
        if (entity.getExpiresAt().isBefore(Instant.now())) {
            throw new ApiException(ErrorCode.INVALID_TOKEN, "Token expired");
        }
        entity.setUsedAt(Instant.now());
        return entity;
    }

    private String link(String path, String token) {
        return appProperties.frontendBaseUrl() + path + "?token=" + token;
    }
}
