package com.standardinsurance.intrack.auth;

import com.standardinsurance.intrack.auth.dto.ForgotPasswordRequestDto;
import com.standardinsurance.intrack.auth.dto.LoginRequestDto;
import com.standardinsurance.intrack.auth.dto.RegisterRequestDto;
import com.standardinsurance.intrack.auth.dto.ResetPasswordRequestDto;
import com.standardinsurance.intrack.user.dto.UserResponseDto;

public interface AuthService {

    AuthResult register(RegisterRequestDto request);

    AuthResult login(LoginRequestDto request);

    AuthResult refresh(String refreshToken);

    void forgotPassword(ForgotPasswordRequestDto request);

    void resetPassword(ResetPasswordRequestDto request);

    UserResponseDto me(String email);
}
