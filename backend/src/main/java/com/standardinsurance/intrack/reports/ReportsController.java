package com.standardinsurance.intrack.reports;

import com.standardinsurance.intrack.reports.dto.ReportsResponseDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ReportsController {

    private final ReportsService reportsService;

    public ReportsController(ReportsService reportsService) {
        this.reportsService = reportsService;
    }

    @GetMapping("/projects/{projectKey}/reports")
    public ReportsResponseDto get(@PathVariable String projectKey) {
        return reportsService.get(projectKey);
    }
}
