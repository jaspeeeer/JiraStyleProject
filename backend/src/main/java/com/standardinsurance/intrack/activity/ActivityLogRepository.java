package com.standardinsurance.intrack.activity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityLogRepository extends JpaRepository<ActivityLogEntity, Long> {
}
