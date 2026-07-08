package com.standardinsurance.intrack.roadmap;

import com.standardinsurance.intrack.roadmap.dto.RoadmapResponseDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class RoadmapController {

    private final RoadmapService roadmapService;

    public RoadmapController(RoadmapService roadmapService) {
        this.roadmapService = roadmapService;
    }

    @GetMapping("/projects/{projectKey}/roadmap")
    public RoadmapResponseDto get(@PathVariable String projectKey) {
        return roadmapService.get(projectKey);
    }
}
