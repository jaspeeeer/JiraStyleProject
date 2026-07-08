package com.standardinsurance.intrack.activity.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCommentRequestDto(
        @NotBlank String body
) {
}
