package com.standardinsurance.intrack.issue;

import com.standardinsurance.intrack.issue.dto.BoardResponseDto;
import com.standardinsurance.intrack.issue.dto.CreateIssueRequestDto;
import com.standardinsurance.intrack.issue.dto.IssueDetailResponseDto;
import com.standardinsurance.intrack.issue.dto.IssueResponseDto;
import com.standardinsurance.intrack.issue.dto.NeighborsDto;
import com.standardinsurance.intrack.issue.dto.UpdateIssueRequestDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class IssueController {

    private final IssueService issueService;

    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    @PostMapping("/issues")
    @ResponseStatus(HttpStatus.CREATED)
    public IssueResponseDto create(@Valid @RequestBody CreateIssueRequestDto request) {
        return issueService.create(request);
    }

    @GetMapping("/issues/{key}")
    public IssueDetailResponseDto get(@PathVariable String key) {
        return issueService.detail(key);
    }

    @GetMapping("/issues/{key}/neighbors")
    public NeighborsDto neighbors(@PathVariable String key) {
        return issueService.neighbors(key);
    }

    @PatchMapping("/issues/{key}")
    public IssueResponseDto update(@PathVariable String key,
                                   @Valid @RequestBody UpdateIssueRequestDto request) {
        return issueService.update(key, request);
    }

    @GetMapping("/projects/{projectKey}/board")
    public BoardResponseDto board(
            @PathVariable String projectKey,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) Long epicId,
            @RequestParam(required = false) IssueType type) {
        return issueService.board(projectKey, assigneeId, priority, epicId, type);
    }
}
