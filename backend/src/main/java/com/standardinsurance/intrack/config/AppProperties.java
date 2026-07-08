package com.standardinsurance.intrack.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * General app settings, bound from {@code intrack.app.*}.
 *
 * @param frontendBaseUrl base URL of the frontend (used in emailed links and as the CORS origin)
 */
@ConfigurationProperties(prefix = "intrack.app")
public record AppProperties(String frontendBaseUrl) {
}
