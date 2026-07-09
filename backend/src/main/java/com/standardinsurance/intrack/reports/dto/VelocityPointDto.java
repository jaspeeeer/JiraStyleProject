package com.standardinsurance.intrack.reports.dto;

import com.standardinsurance.intrack.sprint.SprintStatus;

/**
 * One sprint's velocity: story points completed (DONE issues) vs total points in the sprint.
 */
public record VelocityPointDto(
        Long sprintId,
        String name,
        SprintStatus status,
        int completedPoints,
        int totalPoints
) {
}
