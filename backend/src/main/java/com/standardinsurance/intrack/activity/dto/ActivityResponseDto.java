package com.standardinsurance.intrack.activity.dto;

import java.time.Instant;

public record ActivityResponseDto(
        Long id,
        String action,
        String field,
        String oldValue,
        String newValue,
        String actorName,
        Instant createdAt
) {
}
