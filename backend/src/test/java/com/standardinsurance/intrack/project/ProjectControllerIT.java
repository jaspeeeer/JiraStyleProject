package com.standardinsurance.intrack.project;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.standardinsurance.intrack.project.dto.CreateProjectRequestDto;
import com.standardinsurance.intrack.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
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

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanCreateProject() throws Exception {
        var request = new CreateProjectRequestDto("NEW", "New Project", "desc");

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.key").value("NEW"))
                .andExpect(jsonPath("$.name").value("New Project"))
                .andExpect(jsonPath("$.issueCounter").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void duplicateKeyIsConflict() throws Exception {
        ProjectEntity existing = new ProjectEntity();
        existing.setProjectKey("DUP");
        existing.setName("Existing");
        projectRepository.save(existing);

        var request = new CreateProjectRequestDto("DUP", "Another", null);
        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PROJECT_KEY_TAKEN"));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void nonPrivilegedRoleCannotCreateProject() throws Exception {
        var request = new CreateProjectRequestDto("NOPE", "Nope", null);
        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

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
