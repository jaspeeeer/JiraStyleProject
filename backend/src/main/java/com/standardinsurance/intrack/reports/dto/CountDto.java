package com.standardinsurance.intrack.reports.dto;

/** One bucket of a distribution chart (e.g. status TODO → 4 issues). */
public record CountDto(String label, long count) {
}
