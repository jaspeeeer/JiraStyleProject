package com.standardinsurance.intrack.project;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.standardinsurance.intrack.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * End-to-end slice test: controller -> service -> repository -> DB, then DTO serialization.
 * {@code @Transactional} rolls back after the test (MockMvc runs on the same thread, so the
 * GET sees the row saved within this test's transaction). {@code @WithMockUser} satisfies the
 * "authenticated" requirement introduced with Spring Security in Phase 2.
 */
@AutoConfigureMockMvc
@Transactional
@WithMockUser
class ProjectControllerIT extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ProjectRepository projectRepository;

    @Test
    void listReturnsPersistedProjectsAsDtos() throws Exception {
        ProjectEntity project = new ProjectEntity();
        project.setProjectKey("PROJ");
        project.setName("Platform");
        projectRepository.save(project);

        mockMvc.perform(get("/api/v1/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].key").value("PROJ"))
                .andExpect(jsonPath("$[0].name").value("Platform"))
                .andExpect(jsonPath("$[0].issueCounter").value(0));
    }
}
