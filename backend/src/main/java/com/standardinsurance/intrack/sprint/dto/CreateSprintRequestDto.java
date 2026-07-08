package com.standardinsurance.intrack.sprint.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record CreateSprintRequestDto(
        @NotBlank String projectKey,
        @NotBlank String name,
        String goal,
        LocalDate startDate,
        LocalDate endDate
) {
}
