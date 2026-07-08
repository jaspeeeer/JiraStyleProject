package com.standardinsurance.intrack.roadmap;

import com.standardinsurance.intrack.roadmap.dto.RoadmapResponseDto;

public interface RoadmapService {

    RoadmapResponseDto get(String projectKey);
}
