package com.standardinsurance.intrack.reports;

import com.standardinsurance.intrack.reports.dto.ReportsResponseDto;

public interface ReportsService {

    ReportsResponseDto get(String projectKey);
}
