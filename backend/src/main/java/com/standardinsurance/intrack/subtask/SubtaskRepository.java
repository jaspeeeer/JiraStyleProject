package com.standardinsurance.intrack.subtask;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubtaskRepository extends JpaRepository<SubtaskEntity, Long> {

    List<SubtaskEntity> findByIssueIdOrderByOrderIndexAsc(Long issueId);

    long countByIssueId(Long issueId);
}
