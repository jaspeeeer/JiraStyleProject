package com.standardinsurance.intrack.timeline;

import com.standardinsurance.intrack.timeline.dto.TimelineResponseDto;
import org.springframework.stereotype.Service;

/**
 * Scaffold implementation. Replace with real Gantt/dependency logic when the timeline is built
 * (see docs/specs/timeline.md).
 */
@Service
public class TimelineServiceImpl implements TimelineService {

    @Override
    public TimelineResponseDto get(String projectKey) {
        return new TimelineResponseDto(projectKey, "SCAFFOLD",
                "Timeline is not built yet — planned for a later phase.");
    }
}
