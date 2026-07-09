package com.standardinsurance.intrack.timeline.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Gantt-style timeline: overall date range (nulls when the project has nothing to plot),
 * sprint bands, and one bar per issue.
 */
public record TimelineResponseDto(
        String projectKey,
        LocalDate rangeStart,
        LocalDate rangeEnd,
        List<TimelineSprintDto> sprints,
        List<TimelineItemDto> items
) {
}
