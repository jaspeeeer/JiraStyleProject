package com.standardinsurance.intrack.timeline;

import com.standardinsurance.intrack.timeline.dto.TimelineResponseDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class TimelineController {

    private final TimelineService timelineService;

    public TimelineController(TimelineService timelineService) {
        this.timelineService = timelineService;
    }

    @GetMapping("/projects/{projectKey}/timeline")
    public TimelineResponseDto get(@PathVariable String projectKey) {
        return timelineService.get(projectKey);
    }
}
