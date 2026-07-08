package com.standardinsurance.intrack.issue;

import com.standardinsurance.intrack.issue.dto.BoardResponseDto;
import com.standardinsurance.intrack.issue.dto.CreateIssueRequestDto;
import com.standardinsurance.intrack.issue.dto.IssueDetailResponseDto;
import com.standardinsurance.intrack.issue.dto.IssueResponseDto;
import com.standardinsurance.intrack.issue.dto.NeighborsDto;
import com.standardinsurance.intrack.issue.dto.UpdateIssueRequestDto;

public interface IssueService {

    IssueResponseDto create(CreateIssueRequestDto request);

    IssueDetailResponseDto detail(String key);

    IssueResponseDto update(String key, UpdateIssueRequestDto request);

    NeighborsDto neighbors(String key);

    BoardResponseDto board(String projectKey, Long assigneeId, Priority priority,
                           Long epicId, IssueType type);
}
