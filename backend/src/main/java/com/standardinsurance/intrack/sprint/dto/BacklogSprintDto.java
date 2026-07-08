package com.standardinsurance.intrack.sprint.dto;

import com.standardinsurance.intrack.issue.dto.IssueCardDto;
import java.util.List;

public record BacklogSprintDto(
        SprintResponseDto sprint,
        List<IssueCardDto> issues
) {
}
