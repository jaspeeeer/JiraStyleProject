package com.standardinsurance.intrack.sprint.dto;

import com.standardinsurance.intrack.issue.dto.IssueCardDto;
import java.util.List;

/**
 * Backlog view: non-completed sprints (each with their issues) plus the unscheduled backlog.
 */
public record BacklogResponseDto(
        String projectKey,
        List<BacklogSprintDto> sprints,
        List<IssueCardDto> backlog
) {
}
