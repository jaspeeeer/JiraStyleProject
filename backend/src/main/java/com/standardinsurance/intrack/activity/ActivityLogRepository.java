package com.standardinsurance.intrack.activity;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityLogRepository extends JpaRepository<ActivityLogEntity, Long> {

    List<ActivityLogEntity> findByIssueIdOrderByCreatedAtDesc(Long issueId);
}
