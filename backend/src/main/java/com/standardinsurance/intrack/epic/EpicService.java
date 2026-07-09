package com.standardinsurance.intrack.epic;

import com.standardinsurance.intrack.epic.dto.CreateEpicRequestDto;
import com.standardinsurance.intrack.epic.dto.EpicResponseDto;
import java.util.List;

public interface EpicService {

    List<EpicResponseDto> list(String projectKey);

    EpicResponseDto create(CreateEpicRequestDto request);
}
