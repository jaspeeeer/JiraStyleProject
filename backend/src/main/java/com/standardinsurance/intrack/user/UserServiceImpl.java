package com.standardinsurance.intrack.user;

import com.standardinsurance.intrack.auth.PasswordResetService;
import com.standardinsurance.intrack.common.error.ApiException;
import com.standardinsurance.intrack.common.error.ErrorCode;
import com.standardinsurance.intrack.common.web.PageResponse;
import com.standardinsurance.intrack.user.dto.InviteUserRequestDto;
import com.standardinsurance.intrack.user.dto.RoleResponseDto;
import com.standardinsurance.intrack.user.dto.UpdateUserRequestDto;
import com.standardinsurance.intrack.user.dto.UserResponseDto;
import java.util.Arrays;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordResetService passwordResetService;

    public UserServiceImpl(UserRepository userRepository,
                           UserMapper userMapper,
                           PasswordResetService passwordResetService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordResetService = passwordResetService;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponseDto> list(Role role, UserStatus status, Long projectId,
                                              String q, Pageable pageable) {
        Page<UserResponseDto> page = userRepository.findAll(buildSpec(role, status, projectId, q), pageable)
                .map(userMapper::toResponse);
        return PageResponse.from(page);
    }

    @Override
    public UserResponseDto invite(InviteUserRequestDto request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ApiException(ErrorCode.EMAIL_TAKEN, "Email is already registered");
        }
        UserEntity user = new UserEntity();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setRole(request.role());
        user.setStatus(UserStatus.INVITED);
        userRepository.save(user);

        String token = passwordResetService.issue(user);
        passwordResetService.sendInvite(user, token);
        return userMapper.toResponse(user);
    }

    @Override
    public void resendInvite(Long id) {
        UserEntity user = findUser(id);
        if (user.getStatus() != UserStatus.INVITED) {
            throw new ApiException(ErrorCode.CONFLICT, "User is not in INVITED status");
        }
        String token = passwordResetService.issue(user);
        passwordResetService.sendInvite(user, token);
    }

    @Override
    public UserResponseDto update(Long id, UpdateUserRequestDto request) {
        UserEntity user = findUser(id);
        if (request.role() != null) {
            user.setRole(request.role());
        }
        if (request.status() != null) {
            user.setStatus(request.status());
        }
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public String exportCsv(Role role, UserStatus status, Long projectId, String q) {
        List<UserEntity> users = userRepository.findAll(buildSpec(role, status, projectId, q));
        StringBuilder csv = new StringBuilder("id,name,email,role,status,lastActiveAt\n");
        for (UserEntity user : users) {
            csv.append(field(user.getId()))
                    .append(',').append(field(user.getName()))
                    .append(',').append(field(user.getEmail()))
                    .append(',').append(field(user.getRole()))
                    .append(',').append(field(user.getStatus()))
                    .append(',').append(field(user.getLastActiveAt()))
                    .append('\n');
        }
        return csv.toString();
    }

    @Override
    public List<RoleResponseDto> roles() {
        return Arrays.stream(Role.values()).map(r -> new RoleResponseDto(r.name())).toList();
    }

    private Specification<UserEntity> buildSpec(Role role, UserStatus status, Long projectId, String q) {
        Specification<UserEntity> spec = Specification.where(null);
        if (role != null) {
            spec = spec.and(UserSpecifications.hasRole(role));
        }
        if (status != null) {
            spec = spec.and(UserSpecifications.hasStatus(status));
        }
        if (projectId != null) {
            spec = spec.and(UserSpecifications.memberOfProject(projectId));
        }
        if (StringUtils.hasText(q)) {
            spec = spec.and(UserSpecifications.matches(q));
        }
        return spec;
    }

    private UserEntity findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND, "User " + id + " not found"));
    }

    /** CSV-escapes a field: wrap in quotes, double embedded quotes; empty for null. */
    private static String field(Object value) {
        if (value == null) {
            return "\"\"";
        }
        return '"' + value.toString().replace("\"", "\"\"") + '"';
    }
}
