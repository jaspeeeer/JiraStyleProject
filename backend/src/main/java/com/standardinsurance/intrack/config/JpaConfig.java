package com.standardinsurance.intrack.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables JPA auditing so {@code @CreatedDate}/{@code @LastModifiedDate} on
 * {@link com.standardinsurance.intrack.common.domain.BaseEntity} are populated.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
