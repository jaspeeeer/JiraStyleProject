package com.standardinsurance.intrack.user.dto;

import com.standardinsurance.intrack.user.Role;
import com.standardinsurance.intrack.user.UserStatus;

/**
 * Partial update of a user. Null fields are left unchanged.
 */
public record UpdateUserRequestDto(
        Role role,
        UserStatus status
) {
}
