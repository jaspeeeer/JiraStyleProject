package com.standardinsurance.intrack.sprint;

import com.standardinsurance.intrack.sprint.dto.BacklogResponseDto;
import com.standardinsurance.intrack.sprint.dto.CreateSprintRequestDto;
import com.standardinsurance.intrack.sprint.dto.SprintResponseDto;

public interface SprintService {

    SprintResponseDto create(CreateSprintRequestDto request);

    SprintResponseDto start(Long id);

    SprintResponseDto complete(Long id);

    BacklogResponseDto backlog(String projectKey);
}
