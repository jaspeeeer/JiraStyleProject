package com.standardinsurance.intrack.user.dto;

import com.standardinsurance.intrack.user.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InviteUserRequestDto(
        @NotBlank String name,
        @Email @NotBlank String email,
        @NotNull Role role
) {
}
