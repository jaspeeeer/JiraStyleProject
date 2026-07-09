package com.standardinsurance.intrack.timeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.standardinsurance.intrack.issue.IssueEntity;
import com.standardinsurance.intrack.issue.IssueRepository;
import com.standardinsurance.intrack.issue.IssueStatus;
import com.standardinsurance.intrack.issue.IssueType;
import com.standardinsurance.intrack.issue.Priority;
import com.standardinsurance.intrack.project.ProjectEntity;
import com.standardinsurance.intrack.project.ProjectRepository;
import com.standardinsurance.intrack.sprint.SprintEntity;
import com.standardinsurance.intrack.sprint.SprintRepository;
import com.standardinsurance.intrack.sprint.SprintStatus;
import com.standardinsurance.intrack.timeline.dto.TimelineResponseDto;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TimelineServiceImplTest {

    @Mock ProjectRepository projectRepository;
    @Mock IssueRepository issueRepository;
    @Mock SprintRepository sprintRepository;

    @InjectMocks TimelineServiceImpl timelineService;

    private ProjectEntity project() {
        ProjectEntity project = new ProjectEntity();
        project.setId(1L);
        project.setProjectKey("PROJ");
        return project;
    }

    private static IssueEntity issue(String key, String createdAt, LocalDate dueDate) {
        IssueEntity issue = new IssueEntity();
        issue.setIssueKey(key);
        issue.setTitle(key);
        issue.setStatus(IssueStatus.TODO);
        issue.setType(IssueType.TASK);
        issue.setPriority(Priority.MEDIUM);
        issue.setCreatedAt(Instant.parse(createdAt));
        issue.setDueDate(dueDate);
        return issue;
    }

    @Test
    void computesRangeAndClampsBackdatedDueDates() {
        given(projectRepository.findByProjectKey("PROJ")).willReturn(Optional.of(project()));

        SprintEntity sprint = new SprintEntity();
        sprint.setId(5L);
        sprint.setName("Sprint 1");
        sprint.setStatus(SprintStatus.ACTIVE);
        sprint.setStartDate(LocalDate.parse("2026-06-20"));
        sprint.setEndDate(LocalDate.parse("2026-07-04"));
        given(sprintRepository.findByProjectIdOrderByStartDateAsc(1L)).willReturn(List.of(sprint));

        given(issueRepository.findByProjectIdOrderByIdAsc(1L)).willReturn(List.of(
                issue("PROJ-1", "2026-07-01T10:00:00Z", LocalDate.parse("2026-07-20")),
                issue("PROJ-2", "2026-07-02T10:00:00Z", null),                          // no due date
                issue("PROJ-3", "2026-07-03T10:00:00Z", LocalDate.parse("2026-06-01")))); // backdated

        TimelineResponseDto timeline = timelineService.get("PROJ");

        assertThat(timeline.rangeStart()).isEqualTo(LocalDate.parse("2026-06-20")); // sprint start
        assertThat(timeline.rangeEnd()).isEqualTo(LocalDate.parse("2026-07-20"));   // latest due
        assertThat(timeline.items()).hasSize(3);
        // No due date -> end == start (rendered as a point).
        assertThat(timeline.items().get(1).endDate()).isEqualTo(timeline.items().get(1).startDate());
        // Due before creation is clamped to the start, never a negative-width bar.
        assertThat(timeline.items().get(2).endDate()).isEqualTo(LocalDate.parse("2026-07-03"));
    }

    @Test
    void emptyProjectYieldsNullRange() {
        given(projectRepository.findByProjectKey("PROJ")).willReturn(Optional.of(project()));
        given(sprintRepository.findByProjectIdOrderByStartDateAsc(1L)).willReturn(List.of());
        given(issueRepository.findByProjectIdOrderByIdAsc(1L)).willReturn(List.of());

        TimelineResponseDto timeline = timelineService.get("PROJ");

        assertThat(timeline.rangeStart()).isNull();
        assertThat(timeline.rangeEnd()).isNull();
        assertThat(timeline.items()).isEmpty();
        assertThat(timeline.sprints()).isEmpty();
    }
}
