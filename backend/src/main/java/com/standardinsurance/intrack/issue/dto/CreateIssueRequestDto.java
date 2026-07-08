package com.standardinsurance.intrack.issue.dto;

import com.standardinsurance.intrack.issue.IssueStatus;
import com.standardinsurance.intrack.issue.IssueType;
import com.standardinsurance.intrack.issue.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Create an issue. {@code status} defaults to TODO when omitted (used by inline column adds).
 */
public record CreateIssueRequestDto(
        @NotBlank String projectKey,
        @NotBlank String title,
        String description,
        @NotNull IssueType type,
        @NotNull Priority priority,
        IssueStatus status,
        Integer storyPoints,
        Long assigneeId,
        Long epicId,
        Long sprintId,
        LocalDate dueDate
) {
}
