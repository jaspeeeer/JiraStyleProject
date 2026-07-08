package com.standardinsurance.intrack.issue.dto;

import com.standardinsurance.intrack.issue.IssueStatus;
import com.standardinsurance.intrack.issue.IssueType;
import com.standardinsurance.intrack.issue.Priority;
import java.time.Instant;
import java.time.LocalDate;

public record IssueResponseDto(
        Long id,
        String key,
        String projectKey,
        String title,
        String description,
        IssueStatus status,
        Priority priority,
        IssueType type,
        Integer storyPoints,
        LocalDate dueDate,
        Long assigneeId,
        String assigneeName,
        Long reporterId,
        String reporterName,
        Long epicId,
        String epicName,
        Long sprintId,
        String sprintName,
        Instant createdAt,
        Instant updatedAt
) {
}
