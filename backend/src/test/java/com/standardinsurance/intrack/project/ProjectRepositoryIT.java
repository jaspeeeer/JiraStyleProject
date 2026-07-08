package com.standardinsurance.intrack.project;

import static org.assertj.core.api.Assertions.assertThat;

import com.standardinsurance.intrack.support.AbstractIntegrationTest;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration test: persists a project against a real Postgres (Testcontainers) and verifies
 * the schema (from Flyway) and JPA auditing wiring. {@code @Transactional} rolls back after each
 * test so the shared container DB stays clean between tests.
 */
@Transactional
class ProjectRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    ProjectRepository projectRepository;

    @Test
    void savesAndFindsByProjectKey() {
        ProjectEntity project = new ProjectEntity();
        project.setProjectKey("PROJ");
        project.setName("Platform");

        projectRepository.save(project);

        Optional<ProjectEntity> found = projectRepository.findByProjectKey("PROJ");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Platform");
        assertThat(found.get().getIssueCounter()).isZero();
        assertThat(found.get().getCreatedAt()).isNotNull();
        assertThat(found.get().getUpdatedAt()).isNotNull();
    }
}
