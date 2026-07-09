package com.standardinsurance.intrack.reports;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.standardinsurance.intrack.issue.IssueEntity;
import com.standardinsurance.intrack.issue.IssueRepository;
import com.standardinsurance.intrack.issue.IssueStatus;
import com.standardinsurance.intrack.issue.IssueType;
import com.standardinsurance.intrack.issue.Priority;
import com.standardinsurance.intrack.project.ProjectEntity;
import com.standardinsurance.intrack.project.ProjectRepository;
import com.standardinsurance.intrack.reports.dto.CountDto;
import com.standardinsurance.intrack.reports.dto.ReportsResponseDto;
import com.standardinsurance.intrack.sprint.SprintEntity;
import com.standardinsurance.intrack.sprint.SprintRepository;
import com.standardinsurance.intrack.sprint.SprintStatus;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportsServiceImplTest {

    @Mock ProjectRepository projectRepository;
    @Mock IssueRepository issueRepository;
    @Mock SprintRepository sprintRepository;

    @InjectMocks ReportsServiceImpl reportsService;

    private static IssueEntity issue(IssueStatus status, Priority priority, Integer points) {
        IssueEntity issue = new IssueEntity();
        issue.setStatus(status);
        issue.setPriority(priority);
        issue.setType(IssueType.STORY);
        issue.setStoryPoints(points);
        return issue;
    }

    @Test
    void aggregatesTotalsDistributionsAndVelocity() {
        ProjectEntity project = new ProjectEntity();
        project.setId(1L);
        project.setProjectKey("PROJ");
        given(projectRepository.findByProjectKey("PROJ")).willReturn(Optional.of(project));

        IssueEntity done = issue(IssueStatus.DONE, Priority.HIGH, 5);
        IssueEntity inProgress = issue(IssueStatus.IN_PROGRESS, Priority.HIGH, 3);
        IssueEntity todoNoPoints = issue(IssueStatus.TODO, Priority.LOW, null);
        given(issueRepository.findByProjectIdOrderByIdAsc(1L))
                .willReturn(List.of(done, inProgress, todoNoPoints));

        SprintEntity sprint = new SprintEntity();
        sprint.setId(9L);
        sprint.setName("Sprint 1");
        sprint.setStatus(SprintStatus.ACTIVE);
        given(sprintRepository.findByProjectIdOrderByStartDateAsc(1L)).willReturn(List.of(sprint));
        given(issueRepository.findBySprintId(9L)).willReturn(List.of(done, inProgress));

        ReportsResponseDto reports = reportsService.get("PROJ");

        assertThat(reports.totalIssues()).isEqualTo(3);
        assertThat(reports.doneIssues()).isEqualTo(1);
        assertThat(reports.totalPoints()).isEqualTo(8);   // null points ignored
        assertThat(reports.donePoints()).isEqualTo(5);

        // Every bucket present in enum order, zeros included.
        assertThat(reports.statusCounts()).containsExactly(
                new CountDto("TODO", 1), new CountDto("IN_PROGRESS", 1),
                new CountDto("IN_REVIEW", 0), new CountDto("DONE", 1));
        assertThat(reports.priorityCounts()).containsExactly(
                new CountDto("HIGH", 2), new CountDto("MEDIUM", 0), new CountDto("LOW", 1));

        assertThat(reports.velocity()).hasSize(1);
        assertThat(reports.velocity().get(0).completedPoints()).isEqualTo(5);
        assertThat(reports.velocity().get(0).totalPoints()).isEqualTo(8);
    }
}
