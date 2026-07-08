package com.standardinsurance.intrack.issue.dto;

/**
 * Adjacent issue keys within the same project (by creation order), for prev/next navigation.
 * Either may be null at the ends.
 */
public record NeighborsDto(
        String prev,
        String next
) {
}
