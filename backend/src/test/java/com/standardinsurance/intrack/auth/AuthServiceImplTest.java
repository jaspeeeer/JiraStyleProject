package com.standardinsurance.intrack.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.standardinsurance.intrack.auth.dto.LoginRequestDto;
import com.standardinsurance.intrack.auth.dto.RegisterRequestDto;
import com.standardinsurance.intrack.auth.jwt.JwtTokenProvider;
import com.standardinsurance.intrack.common.error.ApiException;
import com.standardinsurance.intrack.common.error.ErrorCode;
import com.standardinsurance.intrack.user.Role;
import com.standardinsurance.intrack.user.UserEntity;
import com.standardinsurance.intrack.user.UserMapper;
import com.standardinsurance.intrack.user.UserRepository;
import com.standardinsurance.intrack.user.UserStatus;
import com.standardinsurance.intrack.user.dto.UserResponseDto;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtTokenProvider tokenProvider;
    @Mock UserMapper userMapper;
    @Mock PasswordResetService passwordResetService;

    @InjectMocks AuthServiceImpl authService;

    @Test
    void registerRejectsExistingEmail() {
        given(userRepository.existsByEmail("dup@intrack.local")).willReturn(true);

        assertThatThrownBy(() -> authService.register(
                new RegisterRequestDto("Dup", "dup@intrack.local", "password1")))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).errorCode())
                .isEqualTo(ErrorCode.EMAIL_TAKEN);
    }

    @Test
    void loginRejectsUnknownEmail() {
        given(userRepository.findByEmail("nobody@intrack.local")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(
                new LoginRequestDto("nobody@intrack.local", "password1")))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).errorCode())
                .isEqualTo(ErrorCode.BAD_CREDENTIALS);
    }

    @Test
    void loginSucceedsAndIssuesTokens() {
        UserEntity user = new UserEntity();
        user.setEmail("ada@intrack.local");
        user.setPasswordHash("hashed");
        user.setRole(Role.ADMIN);
        user.setStatus(UserStatus.ACTIVE);
        UserResponseDto dto = new UserResponseDto(1L, "Ada", "ada@intrack.local", Role.ADMIN,
                UserStatus.ACTIVE, null);

        given(userRepository.findByEmail("ada@intrack.local")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("password1", "hashed")).willReturn(true);
        given(tokenProvider.generateAccessToken("ada@intrack.local", Role.ADMIN)).willReturn("acc");
        given(tokenProvider.generateRefreshToken("ada@intrack.local")).willReturn("ref");
        given(userMapper.toResponse(any(UserEntity.class))).willReturn(dto);

        AuthResult result = authService.login(new LoginRequestDto("ada@intrack.local", "password1"));

        assertThat(result.accessToken()).isEqualTo("acc");
        assertThat(result.refreshToken()).isEqualTo("ref");
        assertThat(result.user()).isEqualTo(dto);
        assertThat(user.getLastActiveAt()).isNotNull();
    }
}
