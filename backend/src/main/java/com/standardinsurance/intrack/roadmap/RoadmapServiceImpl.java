package com.standardinsurance.intrack.roadmap;

import com.standardinsurance.intrack.roadmap.dto.RoadmapResponseDto;
import org.springframework.stereotype.Service;

/**
 * Scaffold implementation. Replace with real epic/timeframe logic when the roadmap is built
 * (see docs/specs/roadmap.md).
 */
@Service
public class RoadmapServiceImpl implements RoadmapService {

    @Override
    public RoadmapResponseDto get(String projectKey) {
        return new RoadmapResponseDto(projectKey, "SCAFFOLD",
                "Roadmap is not built yet — planned for a later phase.");
    }
}
