package com.standardinsurance.intrack.issue;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IssueRepository
        extends JpaRepository<IssueEntity, Long>, JpaSpecificationExecutor<IssueEntity> {

    Optional<IssueEntity> findByIssueKey(String issueKey);

    List<IssueEntity> findBySprintId(Long sprintId);

    List<IssueEntity> findByProjectIdAndSprintIsNull(Long projectId);

    List<IssueEntity> findByProjectIdOrderByIdAsc(Long projectId);
}
