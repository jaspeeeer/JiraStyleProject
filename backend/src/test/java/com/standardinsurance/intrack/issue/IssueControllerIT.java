package com.standardinsurance.intrack.issue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.standardinsurance.intrack.issue.dto.CreateIssueRequestDto;
import com.standardinsurance.intrack.project.ProjectEntity;
import com.standardinsurance.intrack.project.ProjectRepository;
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
 * End-to-end board slice: create an issue, verify per-project key + board grouping, and filters.
 */
@AutoConfigureMockMvc
@Transactional
@WithMockUser
class IssueControllerIT extends AbstractIntegrationTest {

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

    private void createHighPriorityStory() throws Exception {
        var create = new CreateIssueRequestDto("PROJ", "Build login", null,
                IssueType.STORY, Priority.HIGH, null, 3, null, null, null, null);
        mockMvc.perform(post("/api/v1/issues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.key").value("PROJ-1"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void createdIssueAppearsInTheTodoColumn() throws Exception {
        createHighPriorityStory();

        mockMvc.perform(get("/api/v1/projects/PROJ/board"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.columns[0].status").value("TODO"))
                .andExpect(jsonPath("$.columns[0].count").value(1))
                .andExpect(jsonPath("$.columns[0].cards[0].key").value("PROJ-1"));
    }

    @Test
    void boardFiltersByPriority() throws Exception {
        createHighPriorityStory();

        mockMvc.perform(get("/api/v1/projects/PROJ/board").param("priority", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.columns[0].count").value(1));

        mockMvc.perform(get("/api/v1/projects/PROJ/board").param("priority", "LOW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.columns[0].count").value(0));
    }
}
