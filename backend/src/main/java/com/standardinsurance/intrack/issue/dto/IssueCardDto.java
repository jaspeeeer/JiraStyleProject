package com.standardinsurance.intrack.issue.dto;

import com.standardinsurance.intrack.issue.IssueType;
import com.standardinsurance.intrack.issue.Priority;

/** Condensed issue for a board card. */
public record IssueCardDto(
        Long id,
        String key,
        String title,
        Priority priority,
        IssueType type,
        Integer storyPoints,
        Long assigneeId,
        String assigneeName,
        String epicName
) {
}
