package com.standardinsurance.intrack.epic;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.standardinsurance.intrack.epic.dto.CreateEpicRequestDto;
import com.standardinsurance.intrack.project.ProjectEntity;
import com.standardinsurance.intrack.project.ProjectRepository;
import com.standardinsurance.intrack.support.AbstractIntegrationTest;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@AutoConfigureMockMvc
@Transactional
@WithMockUser(roles = "PROJECT_LEAD")
class EpicControllerIT extends AbstractIntegrationTest {

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
    void createAssignsSequentialKeyAndListReturnsIt() throws Exception {
        var request = new CreateEpicRequestDto("PROJ", "Auth overhaul", "#d4a72c", "desc",
                LocalDate.parse("2026-07-01"), LocalDate.parse("2026-08-15"));

        mockMvc.perform(post("/api/v1/epics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.key").value("E1"))
                .andExpect(jsonPath("$.name").value("Auth overhaul"))
                .andExpect(jsonPath("$.startDate").value("2026-07-01"));

        mockMvc.perform(get("/api/v1/projects/PROJ/epics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].key").value("E1"));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void viewerCannotCreateEpic() throws Exception {
        var request = new CreateEpicRequestDto("PROJ", "Nope", null, null, null, null);
        mockMvc.perform(post("/api/v1/epics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
