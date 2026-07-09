package com.standardinsurance.intrack.timeline;

import com.standardinsurance.intrack.common.error.ApiException;
import com.standardinsurance.intrack.common.error.ErrorCode;
import com.standardinsurance.intrack.issue.IssueRepository;
import com.standardinsurance.intrack.project.ProjectEntity;
import com.standardinsurance.intrack.project.ProjectRepository;
import com.standardinsurance.intrack.sprint.SprintRepository;
import com.standardinsurance.intrack.timeline.dto.TimelineItemDto;
import com.standardinsurance.intrack.timeline.dto.TimelineResponseDto;
import com.standardinsurance.intrack.timeline.dto.TimelineSprintDto;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TimelineServiceImpl implements TimelineService {

    private final ProjectRepository projectRepository;
    private final IssueRepository issueRepository;
    private final SprintRepository sprintRepository;

    public TimelineServiceImpl(ProjectRepository projectRepository,
                               IssueRepository issueRepository,
                               SprintRepository sprintRepository) {
        this.projectRepository = projectRepository;
        this.issueRepository = issueRepository;
        this.sprintRepository = sprintRepository;
    }

    @Override
    public TimelineResponseDto get(String projectKey) {
        ProjectEntity project = projectRepository.findByProjectKey(projectKey)
                .orElseThrow(() -> new ApiException(ErrorCode.PROJECT_NOT_FOUND,
                        "Project " + projectKey + " not found"));

        List<TimelineSprintDto> sprints = sprintRepository
                .findByProjectIdOrderByStartDateAsc(project.getId()).stream()
                .filter(sprint -> sprint.getStartDate() != null && sprint.getEndDate() != null)
                .map(sprint -> new TimelineSprintDto(sprint.getId(), sprint.getName(),
                        sprint.getStatus(), sprint.getStartDate(), sprint.getEndDate()))
                .toList();

        List<TimelineItemDto> items = issueRepository
                .findByProjectIdOrderByIdAsc(project.getId()).stream()
                .map(issue -> {
                    // Creation date in UTC keeps the timeline deterministic across timezones.
                    LocalDate start = LocalDate.ofInstant(issue.getCreatedAt(), ZoneOffset.UTC);
                    LocalDate due = issue.getDueDate();
                    // Guard against a due date before creation (imported/backdated data).
                    LocalDate end = (due == null || due.isBefore(start)) ? start : due;
                    return new TimelineItemDto(
                            issue.getIssueKey(),
                            issue.getTitle(),
                            issue.getStatus(),
                            issue.getType(),
                            issue.getEpic() != null ? issue.getEpic().getName() : null,
                            start,
                            end);
                })
                .toList();

        LocalDate rangeStart = Stream.concat(
                        sprints.stream().map(TimelineSprintDto::startDate),
                        items.stream().map(TimelineItemDto::startDate))
                .min(Comparator.naturalOrder())
                .orElse(null);
        LocalDate rangeEnd = Stream.concat(
                        sprints.stream().map(TimelineSprintDto::endDate),
                        items.stream().map(TimelineItemDto::endDate))
                .max(Comparator.naturalOrder())
                .orElse(null);

        return new TimelineResponseDto(project.getProjectKey(), rangeStart, rangeEnd, sprints, items);
    }
}
