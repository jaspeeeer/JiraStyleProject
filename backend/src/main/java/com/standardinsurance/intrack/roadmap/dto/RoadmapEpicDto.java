package com.standardinsurance.intrack.roadmap.dto;

import java.time.LocalDate;

/**
 * An epic as shown on the roadmap: its planned timeframe plus issue progress.
 */
public record RoadmapEpicDto(
        Long id,
        String key,
        String name,
        String color,
        LocalDate startDate,
        LocalDate endDate,
        int totalIssues,
        int doneIssues
) {
}
