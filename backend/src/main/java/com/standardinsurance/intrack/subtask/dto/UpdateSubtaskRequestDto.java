package com.standardinsurance.intrack.subtask.dto;

/**
 * Partial update of a subtask. Null fields are left unchanged.
 */
public record UpdateSubtaskRequestDto(
        String title,
        Boolean done,
        Long assigneeId
) {
}
