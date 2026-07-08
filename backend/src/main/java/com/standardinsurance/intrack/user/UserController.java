package com.standardinsurance.intrack.user;

import com.standardinsurance.intrack.common.web.PageResponse;
import com.standardinsurance.intrack.user.dto.InviteUserRequestDto;
import com.standardinsurance.intrack.user.dto.RoleResponseDto;
import com.standardinsurance.intrack.user.dto.UpdateUserRequestDto;
import com.standardinsurance.intrack.user.dto.UserResponseDto;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * User administration. All operations require the ADMIN role (method security).
 */
@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public PageResponse<UserResponseDto> list(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20) Pageable pageable) {
        return userService.list(role, status, projectId, q, pageable);
    }

    @PostMapping("/users/invite")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDto invite(@Valid @RequestBody InviteUserRequestDto request) {
        return userService.invite(request);
    }

    @PostMapping("/users/{id}/resend-invite")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resendInvite(@PathVariable Long id) {
        userService.resendInvite(id);
    }

    @PatchMapping("/users/{id}")
    public UserResponseDto update(@PathVariable Long id,
                                  @Valid @RequestBody UpdateUserRequestDto request) {
        return userService.update(id, request);
    }

    @GetMapping("/users/export")
    public ResponseEntity<String> export(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String q) {
        String csv = userService.exportCsv(role, status, projectId, q);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"users.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/roles")
    public List<RoleResponseDto> roles() {
        return userService.roles();
    }
}
