package com.standardinsurance.intrack.issue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.standardinsurance.intrack.activity.ActivityLogEntity;
import com.standardinsurance.intrack.activity.ActivityLogRepository;
import com.standardinsurance.intrack.epic.EpicRepository;
import com.standardinsurance.intrack.issue.dto.CreateIssueRequestDto;
import com.standardinsurance.intrack.issue.dto.UpdateIssueRequestDto;
import com.standardinsurance.intrack.project.ProjectEntity;
import com.standardinsurance.intrack.project.ProjectRepository;
import com.standardinsurance.intrack.sprint.SprintRepository;
import com.standardinsurance.intrack.user.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IssueServiceImplTest {

    @Mock IssueRepository issueRepository;
    @Mock ProjectRepository projectRepository;
    @Mock UserRepository userRepository;
    @Mock EpicRepository epicRepository;
    @Mock SprintRepository sprintRepository;
    @Mock ActivityLogRepository activityLogRepository;
    @Mock IssueMapper issueMapper;

    @InjectMocks IssueServiceImpl issueService;

    @Captor ArgumentCaptor<IssueEntity> issueCaptor;

    @Test
    void createGeneratesPerProjectKeyAndIncrementsCounter() {
        ProjectEntity project = new ProjectEntity();
        project.setProjectKey("PROJ");
        project.setIssueCounter(4);
        given(projectRepository.findByProjectKey("PROJ")).willReturn(Optional.of(project));

        var request = new CreateIssueRequestDto("PROJ", "Build login", null,
                IssueType.STORY, Priority.HIGH, null, 3, null, null, null, null);
        issueService.create(request);

        verify(issueRepository).save(issueCaptor.capture());
        assertThat(issueCaptor.getValue().getIssueKey()).isEqualTo("PROJ-5");
        assertThat(issueCaptor.getValue().getStatus()).isEqualTo(IssueStatus.TODO);
        assertThat(project.getIssueCounter()).isEqualTo(5);
    }

    @Test
    void statusChangeIsPersistedAndLogged() {
        IssueEntity issue = new IssueEntity();
        issue.setIssueKey("PROJ-1");
        issue.setStatus(IssueStatus.TODO);
        given(issueRepository.findByIssueKey("PROJ-1")).willReturn(Optional.of(issue));

        var request = new UpdateIssueRequestDto(null, null, IssueStatus.IN_PROGRESS,
                null, null, null, null, null, null, null);
        issueService.update("PROJ-1", request);

        assertThat(issue.getStatus()).isEqualTo(IssueStatus.IN_PROGRESS);
        verify(activityLogRepository).save(any(ActivityLogEntity.class));
    }
}
