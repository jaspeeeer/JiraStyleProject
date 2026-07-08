package com.standardinsurance.intrack.user;

import com.standardinsurance.intrack.common.web.PageResponse;
import com.standardinsurance.intrack.user.dto.InviteUserRequestDto;
import com.standardinsurance.intrack.user.dto.RoleResponseDto;
import com.standardinsurance.intrack.user.dto.UpdateUserRequestDto;
import com.standardinsurance.intrack.user.dto.UserResponseDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface UserService {

    PageResponse<UserResponseDto> list(Role role, UserStatus status, Long projectId, String q, Pageable pageable);

    UserResponseDto invite(InviteUserRequestDto request);

    void resendInvite(Long id);

    UserResponseDto update(Long id, UpdateUserRequestDto request);

    String exportCsv(Role role, UserStatus status, Long projectId, String q);

    List<RoleResponseDto> roles();
}
