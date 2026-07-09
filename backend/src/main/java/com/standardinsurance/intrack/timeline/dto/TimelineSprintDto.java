package com.standardinsurance.intrack.timeline.dto;

import com.standardinsurance.intrack.sprint.SprintStatus;
import java.time.LocalDate;

/** A sprint band on the timeline (only sprints with both dates are included). */
public record TimelineSprintDto(
        Long id,
        String name,
        SprintStatus status,
        LocalDate startDate,
        LocalDate endDate
) {
}
