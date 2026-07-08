package com.standardinsurance.intrack.sprint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.standardinsurance.intrack.common.error.ApiException;
import com.standardinsurance.intrack.common.error.ErrorCode;
import com.standardinsurance.intrack.issue.IssueEntity;
import com.standardinsurance.intrack.issue.IssueMapper;
import com.standardinsurance.intrack.issue.IssueRepository;
import com.standardinsurance.intrack.issue.IssueStatus;
import com.standardinsurance.intrack.project.ProjectEntity;
import com.standardinsurance.intrack.project.ProjectRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SprintServiceImplTest {

    @Mock SprintRepository sprintRepository;
    @Mock ProjectRepository projectRepository;
    @Mock IssueRepository issueRepository;
    @Mock SprintMapper sprintMapper;
    @Mock IssueMapper issueMapper;

    @InjectMocks SprintServiceImpl sprintService;

    @Captor ArgumentCaptor<Integer> totalCaptor;
    @Captor ArgumentCaptor<Integer> doneCaptor;
    @Captor ArgumentCaptor<Integer> pointsCaptor;

    private SprintEntity sprintInProject(long sprintId, long projectId, SprintStatus status) {
        ProjectEntity project = new ProjectEntity();
        project.setId(projectId);
        SprintEntity sprint = new SprintEntity();
        sprint.setId(sprintId);
        sprint.setProject(project);
        sprint.setStatus(status);
        return sprint;
    }

    private IssueEntity issue(IssueStatus status, Integer points) {
        IssueEntity issue = new IssueEntity();
        issue.setStatus(status);
        issue.setStoryPoints(points);
        return issue;
    }

    @Test
    void startActivatesSprintAndComputesProgress() {
        SprintEntity sprint = sprintInProject(1L, 10L, SprintStatus.PLANNED);
        given(sprintRepository.findById(1L)).willReturn(Optional.of(sprint));
        given(sprintRepository.existsByProjectIdAndStatus(10L, SprintStatus.ACTIVE)).willReturn(false);
        given(issueRepository.findBySprintId(1L)).willReturn(List.of(
                issue(IssueStatus.DONE, 3), issue(IssueStatus.TODO, 2)));

        sprintService.start(1L);

        assertThat(sprint.getStatus()).isEqualTo(SprintStatus.ACTIVE);
        org.mockito.Mockito.verify(sprintMapper).toResponse(eq(sprint),
                totalCaptor.capture(), doneCaptor.capture(), pointsCaptor.capture());
        assertThat(totalCaptor.getValue()).isEqualTo(2);
        assertThat(doneCaptor.getValue()).isEqualTo(1);
        assertThat(pointsCaptor.getValue()).isEqualTo(5);
    }

    @Test
    void startRejectedWhenAnotherSprintIsActive() {
        SprintEntity sprint = sprintInProject(2L, 10L, SprintStatus.PLANNED);
        given(sprintRepository.findById(2L)).willReturn(Optional.of(sprint));
        given(sprintRepository.existsByProjectIdAndStatus(10L, SprintStatus.ACTIVE)).willReturn(true);

        assertThatThrownBy(() -> sprintService.start(2L))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).errorCode())
                .isEqualTo(ErrorCode.SPRINT_ALREADY_ACTIVE);
        assertThat(sprint.getStatus()).isEqualTo(SprintStatus.PLANNED);
    }

    @Test
    void completeMovesUnfinishedIssuesToBacklog() {
        SprintEntity sprint = sprintInProject(3L, 10L, SprintStatus.ACTIVE);
        IssueEntity done = issue(IssueStatus.DONE, 3);
        IssueEntity todo = issue(IssueStatus.TODO, 2);
        done.setSprint(sprint);
        todo.setSprint(sprint);
        given(sprintRepository.findById(3L)).willReturn(Optional.of(sprint));
        given(issueRepository.findBySprintId(3L)).willReturn(List.of(done, todo));

        sprintService.complete(3L);

        assertThat(sprint.getStatus()).isEqualTo(SprintStatus.COMPLETED);
        assertThat(todo.getSprint()).isNull();     // unfinished -> back to backlog
        assertThat(done.getSprint()).isEqualTo(sprint); // finished stays
    }

    @Test
    void completeRejectedWhenSprintNotActive() {
        SprintEntity sprint = sprintInProject(4L, 10L, SprintStatus.PLANNED);
        given(sprintRepository.findById(4L)).willReturn(Optional.of(sprint));

        assertThatThrownBy(() -> sprintService.complete(4L))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).errorCode())
                .isEqualTo(ErrorCode.SPRINT_NOT_ACTIVE);
    }
}
