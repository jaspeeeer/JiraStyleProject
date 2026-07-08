package com.standardinsurance.intrack.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Create a project. The {@code key} is the short prefix used for issue keys (e.g. {@code PROJ}
 * → {@code PROJ-1}); it must be 2–10 uppercase letters/digits and unique.
 */
public record CreateProjectRequestDto(
        @NotBlank
        @Pattern(regexp = "^[A-Z][A-Z0-9]{1,9}$",
                message = "Key must be 2-10 uppercase letters/digits, starting with a letter")
        String key,

        @NotBlank @Size(max = 255) String name,

        String description
) {
}
