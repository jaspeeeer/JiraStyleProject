package com.standardinsurance.intrack.subtask.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateSubtaskRequestDto(
        @NotBlank String title,
        Long assigneeId
) {
}
