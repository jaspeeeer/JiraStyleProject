package com.standardinsurance.intrack.activity.dto;

import java.time.Instant;

public record CommentResponseDto(
        Long id,
        String body,
        Long authorId,
        String authorName,
        Instant createdAt
) {
}
