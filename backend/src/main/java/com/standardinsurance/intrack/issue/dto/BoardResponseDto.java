package com.standardinsurance.intrack.issue.dto;

import java.util.List;

public record BoardResponseDto(
        String projectKey,
        List<BoardColumnDto> columns
) {
}
