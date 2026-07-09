package com.standardinsurance.intrack.epic.dto;

import java.time.LocalDate;

public record EpicResponseDto(
        Long id,
        String key,
        String name,
        String color,
        String description,
        LocalDate startDate,
        LocalDate endDate
) {
}
