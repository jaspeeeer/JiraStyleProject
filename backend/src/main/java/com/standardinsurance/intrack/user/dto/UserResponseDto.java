package com.standardinsurance.intrack.user.dto;

import com.standardinsurance.intrack.user.Role;
import com.standardinsurance.intrack.user.UserStatus;
import java.time.Instant;

public record UserResponseDto(
        Long id,
        String name,
        String email,
        Role role,
        UserStatus status,
        Instant lastActiveAt
) {
}
