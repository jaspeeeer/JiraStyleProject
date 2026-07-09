package com.standardinsurance.intrack.reports.dto;

import java.util.List;

/**
 * Project analytics: headline totals, issue distributions (every enum bucket present, zeros
 * included, in enum order), and per-sprint velocity.
 */
public record ReportsResponseDto(
        String projectKey,
        long totalIssues,
        long doneIssues,
        long totalPoints,
        long donePoints,
        List<CountDto> statusCounts,
        List<CountDto> priorityCounts,
        List<CountDto> typeCounts,
        List<VelocityPointDto> velocity
) {
}
