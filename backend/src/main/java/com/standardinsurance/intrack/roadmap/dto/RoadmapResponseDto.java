package com.standardinsurance.intrack.roadmap.dto;

import java.util.List;

/**
 * Roadmap view: every epic in the project with its timeframe and progress. Epics without dates
 * are still included — the UI groups them as "unscheduled".
 */
public record RoadmapResponseDto(
        String projectKey,
        List<RoadmapEpicDto> epics
) {
}
