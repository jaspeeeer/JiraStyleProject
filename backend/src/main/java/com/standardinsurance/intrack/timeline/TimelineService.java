package com.standardinsurance.intrack.timeline;

import com.standardinsurance.intrack.timeline.dto.TimelineResponseDto;

public interface TimelineService {

    TimelineResponseDto get(String projectKey);
}
