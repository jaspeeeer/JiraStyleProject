package com.standardinsurance.intrack.project.dto;

/**
 * Response view of a project. Note {@code key} maps from the entity's {@code projectKey}.
 */
public record ProjectResponseDto(
        Long id,
        String key,
        String name,
        String description,
        int issueCounter
) {
}
