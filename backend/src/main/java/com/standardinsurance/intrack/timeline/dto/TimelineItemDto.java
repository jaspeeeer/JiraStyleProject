package com.standardinsurance.intrack.timeline.dto;

import com.standardinsurance.intrack.issue.IssueStatus;
import com.standardinsurance.intrack.issue.IssueType;
import java.time.LocalDate;

/**
 * An issue bar on the timeline. {@code startDate} is the creation date; {@code endDate} is the
 * due date, or the start date again when no due date is set (rendered as a point).
 */
public record TimelineItemDto(
        String key,
        String title,
        IssueStatus status,
        IssueType type,
        String epicName,
        LocalDate startDate,
        LocalDate endDate
) {
}
