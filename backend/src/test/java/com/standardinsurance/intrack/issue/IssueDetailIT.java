package com.standardinsurance.intrack.issue;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.standardinsurance.intrack.issue.dto.CreateIssueRequestDto;
import com.standardinsurance.intrack.project.ProjectEntity;
import com.standardinsurance.intrack.project.ProjectRepository;
import com.standardinsurance.intrack.support.AbstractIntegrationTest;
import com.standardinsurance.intrack.user.Role;
import com.standardinsurance.intrack.user.UserEntity;
import com.standardinsurance.intrack.user.UserRepository;
import com.standardinsurance.intrack.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Issue-detail aggregate, subtask + comment writes, and prev/next neighbors, end-to-end.
 * The mock principal matches a seeded user so comment authorship resolves.
 */
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "ada@intrack.local", roles = "DEVELOPER")
class IssueDetailIT extends AbstractIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired ProjectRepository projectRepository;
    @Autowired UserRepository userRepository;

    @BeforeEach
    void seed() {
        ProjectEntity project = new ProjectEntity();
        project.setProjectKey("PROJ");
        project.setName("Platform");
        projectRepository.save(project);

        UserEntity ada = new UserEntity();
        ada.setName("Ada");
        ada.setEmail("ada@intrack.local");
        ada.setRole(Role.DEVELOPER);
        ada.setStatus(UserStatus.ACTIVE);
        userRepository.save(ada);
    }

    private String createIssue(String title) throws Exception {
        var create = new CreateIssueRequestDto("PROJ", title, null,
                IssueType.STORY, Priority.MEDIUM, null, null, null, null, null, null);
        String body = mockMvc.perform(post("/api/v1/issues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("key").asText();
    }

    @Test
    void detailAggregatesSubtaskCommentAndProgress() throws Exception {
        String key = createIssue("Build login");

        String subtaskBody = mockMvc.perform(post("/api/v1/issues/{key}/subtasks", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Wire form\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long subtaskId = objectMapper.readTree(subtaskBody).get("id").asLong();

        mockMvc.perform(patch("/api/v1/subtasks/{id}", subtaskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"done\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.done").value(true));

        mockMvc.perform(post("/api/v1/issues/{key}/comments", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\":\"Looks good\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authorName").value("Ada"));

        mockMvc.perform(get("/api/v1/issues/{key}", key))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.issue.key").value(key))
                .andExpect(jsonPath("$.subtasksTotal").value(1))
                .andExpect(jsonPath("$.subtasksDone").value(1))
                .andExpect(jsonPath("$.comments[0].body").value("Looks good"))
                .andExpect(jsonPath("$.activity").isArray());
    }

    @Test
    void neighborsLinkAdjacentIssues() throws Exception {
        String first = createIssue("First");
        String second = createIssue("Second");

        mockMvc.perform(get("/api/v1/issues/{key}/neighbors", first))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prev").value(nullValue()))
                .andExpect(jsonPath("$.next").value(second));

        mockMvc.perform(get("/api/v1/issues/{key}/neighbors", second))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prev").value(first))
                .andExpect(jsonPath("$.next").value(nullValue()));
    }
}
