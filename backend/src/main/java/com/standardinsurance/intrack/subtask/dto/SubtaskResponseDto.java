package com.standardinsurance.intrack.subtask.dto;

public record SubtaskResponseDto(
        Long id,
        String title,
        boolean done,
        Long assigneeId,
        String assigneeName,
        int orderIndex
) {
}
