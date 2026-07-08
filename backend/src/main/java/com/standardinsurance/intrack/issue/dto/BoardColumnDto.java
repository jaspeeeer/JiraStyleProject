package com.standardinsurance.intrack.issue.dto;

import com.standardinsurance.intrack.issue.IssueStatus;
import java.util.List;

public record BoardColumnDto(
        IssueStatus status,
        int count,
        List<IssueCardDto> cards
) {
}
