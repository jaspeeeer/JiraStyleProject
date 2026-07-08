package com.standardinsurance.intrack.reports;

import com.standardinsurance.intrack.reports.dto.ReportsResponseDto;
import org.springframework.stereotype.Service;

/**
 * Scaffold implementation. Replace with real velocity/burndown/throughput logic when reports are
 * built (see docs/specs/reports.md).
 */
@Service
public class ReportsServiceImpl implements ReportsService {

    @Override
    public ReportsResponseDto get(String projectKey) {
        return new ReportsResponseDto(projectKey, "SCAFFOLD",
                "Reports are not built yet — planned for a later phase.");
    }
}
