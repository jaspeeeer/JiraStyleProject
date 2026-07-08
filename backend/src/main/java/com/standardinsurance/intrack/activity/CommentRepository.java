package com.standardinsurance.intrack.activity;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    List<CommentEntity> findByIssueIdOrderByCreatedAtAsc(Long issueId);
}
