package com.standardinsurance.intrack.sprint;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.standardinsurance.intrack.project.ProjectEntity;
import com.standardinsurance.intrack.project.ProjectRepository;
import com.standardinsurance.intrack.sprint.dto.CreateSprintRequestDto;
import com.standardinsurance.intrack.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sprint lifecycle end-to-end: create -> start -> complete, and the single-active-sprint rule.
 */
@AutoConfigureMockMvc
@Transactional
@WithMockUser(roles = "PROJECT_LEAD")
class SprintControllerIT extends AbstractIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired ProjectRepository projectRepository;

    @BeforeEach
    void seedProject() {
        ProjectEntity project = new ProjectEntity();
        project.setProjectKey("PROJ");
        project.setName("Platform");
        projectRepository.save(project);
    }

    private long createSprint(String name) throws Exception {
        var request = new CreateSprintRequestDto("PROJ", name, "goal", null, null);
        String body = mockMvc.perform(post("/api/v1/sprints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PLANNED"))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    @Test
    void createStartCompleteLifecycle() throws Exception {
        long id = createSprint("Sprint 1");

        mockMvc.perform(post("/api/v1/sprints/{id}/start", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mockMvc.perform(post("/api/v1/sprints/{id}/complete", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void onlyOneSprintMayBeActivePerProject() throws Exception {
        long first = createSprint("Sprint 1");
        long second = createSprint("Sprint 2");

        mockMvc.perform(post("/api/v1/sprints/{id}/start", first))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/sprints/{id}/start", second))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SPRINT_ALREADY_ACTIVE"));
    }

    @Test
    void backlogReturnsSprintsAndUnscheduled() throws Exception {
        createSprint("Sprint 1");

        mockMvc.perform(get("/api/v1/projects/PROJ/backlog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectKey").value("PROJ"))
                .andExpect(jsonPath("$.sprints[0].sprint.name").value("Sprint 1"))
                .andExpect(jsonPath("$.backlog").isArray());
    }
}
