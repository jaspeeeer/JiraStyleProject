package com.standardinsurance.intrack.subtask;

import com.standardinsurance.intrack.subtask.dto.CreateSubtaskRequestDto;
import com.standardinsurance.intrack.subtask.dto.SubtaskResponseDto;
import com.standardinsurance.intrack.subtask.dto.UpdateSubtaskRequestDto;

public interface SubtaskService {

    SubtaskResponseDto create(String issueKey, CreateSubtaskRequestDto request);

    SubtaskResponseDto update(Long id, UpdateSubtaskRequestDto request);

    void delete(Long id);
}
