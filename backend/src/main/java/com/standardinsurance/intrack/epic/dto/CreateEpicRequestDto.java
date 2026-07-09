package com.standardinsurance.intrack.epic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateEpicRequestDto(
        @NotBlank String projectKey,
        @NotBlank @Size(max = 255) String name,
        String color,
        String description,
        LocalDate startDate,
        LocalDate endDate
) {
}
