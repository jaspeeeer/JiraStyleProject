package com.standardinsurance.intrack.activity;

import com.standardinsurance.intrack.activity.dto.ActivityResponseDto;
import java.util.List;

public interface ActivityService {

    List<ActivityResponseDto> list(String issueKey);
}
