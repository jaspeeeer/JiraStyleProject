package com.standardinsurance.intrack.auth;

import com.standardinsurance.intrack.auth.dto.ForgotPasswordRequestDto;
import com.standardinsurance.intrack.auth.dto.LoginRequestDto;
import com.standardinsurance.intrack.auth.dto.RegisterRequestDto;
import com.standardinsurance.intrack.auth.dto.ResetPasswordRequestDto;
import com.standardinsurance.intrack.auth.jwt.JwtTokenProvider;
import com.standardinsurance.intrack.common.error.ApiException;
import com.standardinsurance.intrack.common.error.ErrorCode;
import com.standardinsurance.intrack.user.Role;
import com.standardinsurance.intrack.user.UserEntity;
import com.standardinsurance.intrack.user.UserMapper;
import com.standardinsurance.intrack.user.UserRepository;
import com.standardinsurance.intrack.user.UserStatus;
import com.standardinsurance.intrack.user.dto.UserResponseDto;
import java.time.Instant;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final UserMapper userMapper;
    private final PasswordResetService passwordResetService;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider tokenProvider,
                           UserMapper userMapper,
                           PasswordResetService passwordResetService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.userMapper = userMapper;
        this.passwordResetService = passwordResetService;
    }

    @Override
    public AuthResult register(RegisterRequestDto request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ApiException(ErrorCode.EMAIL_TAKEN, "Email is already registered");
        }
        UserEntity user = new UserEntity();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.DEVELOPER);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        return issueTokens(user);
    }

    @Override
    public AuthResult login(LoginRequestDto request) {
        UserEntity user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ApiException(ErrorCode.BAD_CREDENTIALS, "Invalid email or password"));
        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ApiException(ErrorCode.BAD_CREDENTIALS, "Invalid email or password");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ApiException(ErrorCode.BAD_CREDENTIALS, "Account is not active");
        }
        user.setLastActiveAt(Instant.now());
        return issueTokens(user);
    }

    @Override
    public AuthResult refresh(String refreshToken) {
        if (refreshToken == null
                || !tokenProvider.isValid(refreshToken, JwtTokenProvider.TYPE_REFRESH)) {
            throw new ApiException(ErrorCode.INVALID_TOKEN, "Invalid refresh token");
        }
        String email = tokenProvider.getSubject(refreshToken);
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_TOKEN, "Invalid refresh token"));
        String access = tokenProvider.generateAccessToken(user.getEmail(), user.getRole());
        return new AuthResult(access, refreshToken, userMapper.toResponse(user));
    }

    @Override
    public void forgotPassword(ForgotPasswordRequestDto request) {
        // Silent if the email is unknown, to avoid leaking which addresses exist.
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            String token = passwordResetService.issue(user);
            passwordResetService.sendPasswordReset(user, token);
        });
    }

    @Override
    public void resetPassword(ResetPasswordRequestDto request) {
        PasswordResetTokenEntity token = passwordResetService.consume(request.token());
        UserEntity user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        if (user.getStatus() == UserStatus.INVITED) {
            user.setStatus(UserStatus.ACTIVE); // accepting an invite activates the account
        }
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto me(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND, "User not found"));
        return userMapper.toResponse(user);
    }

    private AuthResult issueTokens(UserEntity user) {
        String access = tokenProvider.generateAccessToken(user.getEmail(), user.getRole());
        String refresh = tokenProvider.generateRefreshToken(user.getEmail());
        return new AuthResult(access, refresh, userMapper.toResponse(user));
    }
}
