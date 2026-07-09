package com.standardinsurance.intrack.roadmap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.standardinsurance.intrack.epic.dto.CreateEpicRequestDto;
import com.standardinsurance.intrack.issue.IssueType;
import com.standardinsurance.intrack.issue.Priority;
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
 * Roadmap end-to-end: create an epic, attach issues, complete one, and verify the roadmap
 * aggregates timeframe + progress.
 */
@AutoConfigureMockMvc
@Transactional
@WithMockUser(roles = "PROJECT_LEAD")
class RoadmapControllerIT extends AbstractIntegrationTest {

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

    @Test
    void roadmapAggregatesEpicProgress() throws Exception {
        var epicRequest = new CreateEpicRequestDto("PROJ", "Auth overhaul", null, null,
                java.time.LocalDate.parse("2026-07-01"), java.time.LocalDate.parse("2026-08-15"));
        String epicBody = mockMvc.perform(post("/api/v1/epics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(epicRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long epicId = objectMapper.readTree(epicBody).get("id").asLong();

        for (String title : new String[] {"Login", "Logout"}) {
            var issueRequest = new CreateIssueRequestDto("PROJ", title, null,
                    IssueType.STORY, Priority.MEDIUM, null, 3, null, epicId, null, null);
            mockMvc.perform(post("/api/v1/issues")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(issueRequest)))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(patch("/api/v1/issues/PROJ-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DONE\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/projects/PROJ/roadmap"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectKey").value("PROJ"))
                .andExpect(jsonPath("$.epics[0].key").value("E1"))
                .andExpect(jsonPath("$.epics[0].totalIssues").value(2))
                .andExpect(jsonPath("$.epics[0].doneIssues").value(1))
                .andExpect(jsonPath("$.epics[0].startDate").value("2026-07-01"));
    }
}
