package com.standardinsurance.intrack.project;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {

    Optional<ProjectEntity> findByProjectKey(String projectKey);

    boolean existsByProjectKey(String projectKey);
}
