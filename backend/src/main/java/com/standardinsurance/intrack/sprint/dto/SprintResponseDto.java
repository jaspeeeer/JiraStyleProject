package com.standardinsurance.intrack.sprint.dto;

import com.standardinsurance.intrack.sprint.SprintStatus;
import java.time.LocalDate;

/**
 * Sprint plus computed progress ({@code doneIssues}/{@code totalIssues}) and point totals.
 */
public record SprintResponseDto(
        Long id,
        String name,
        String goal,
        SprintStatus status,
        LocalDate startDate,
        LocalDate endDate,
        int totalIssues,
        int doneIssues,
        int totalPoints
) {
}
