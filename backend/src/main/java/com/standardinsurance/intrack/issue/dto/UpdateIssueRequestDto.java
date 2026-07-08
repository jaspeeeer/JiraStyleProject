package com.standardinsurance.intrack.issue.dto;

import com.standardinsurance.intrack.issue.IssueStatus;
import com.standardinsurance.intrack.issue.IssueType;
import com.standardinsurance.intrack.issue.Priority;
import java.time.LocalDate;

/**
 * Partial update of an issue. Null fields are left unchanged. A drag-and-drop between board
 * columns sends only {@code status}.
 */
public record UpdateIssueRequestDto(
        String title,
        String description,
        IssueStatus status,
        Priority priority,
        IssueType type,
        Integer storyPoints,
        LocalDate dueDate,
        Long assigneeId,
        Long epicId,
        Long sprintId
) {
}
