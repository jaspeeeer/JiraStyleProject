package com.standardinsurance.intrack.sprint;

import com.standardinsurance.intrack.sprint.dto.BacklogResponseDto;
import com.standardinsurance.intrack.sprint.dto.CreateSprintRequestDto;
import com.standardinsurance.intrack.sprint.dto.SprintResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class SprintController {

    private final SprintService sprintService;

    public SprintController(SprintService sprintService) {
        this.sprintService = sprintService;
    }

    @GetMapping("/projects/{projectKey}/backlog")
    public BacklogResponseDto backlog(@PathVariable String projectKey) {
        return sprintService.backlog(projectKey);
    }

    @PostMapping("/sprints")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','PROJECT_LEAD')")
    public SprintResponseDto create(@Valid @RequestBody CreateSprintRequestDto request) {
        return sprintService.create(request);
    }

    @PostMapping("/sprints/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN','PROJECT_LEAD')")
    public SprintResponseDto start(@PathVariable Long id) {
        return sprintService.start(id);
    }

    @PostMapping("/sprints/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN','PROJECT_LEAD')")
    public SprintResponseDto complete(@PathVariable Long id) {
        return sprintService.complete(id);
    }
}
