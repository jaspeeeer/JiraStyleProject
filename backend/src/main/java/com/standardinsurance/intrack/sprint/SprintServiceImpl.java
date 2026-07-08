package com.standardinsurance.intrack.sprint;

import com.standardinsurance.intrack.common.error.ApiException;
import com.standardinsurance.intrack.common.error.ErrorCode;
import com.standardinsurance.intrack.issue.IssueEntity;
import com.standardinsurance.intrack.issue.IssueMapper;
import com.standardinsurance.intrack.issue.IssueRepository;
import com.standardinsurance.intrack.issue.IssueStatus;
import com.standardinsurance.intrack.project.ProjectEntity;
import com.standardinsurance.intrack.project.ProjectRepository;
import com.standardinsurance.intrack.sprint.dto.BacklogResponseDto;
import com.standardinsurance.intrack.sprint.dto.BacklogSprintDto;
import com.standardinsurance.intrack.sprint.dto.CreateSprintRequestDto;
import com.standardinsurance.intrack.sprint.dto.SprintResponseDto;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SprintServiceImpl implements SprintService {

    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;
    private final IssueRepository issueRepository;
    private final SprintMapper sprintMapper;
    private final IssueMapper issueMapper;

    public SprintServiceImpl(SprintRepository sprintRepository,
                             ProjectRepository projectRepository,
                             IssueRepository issueRepository,
                             SprintMapper sprintMapper,
                             IssueMapper issueMapper) {
        this.sprintRepository = sprintRepository;
        this.projectRepository = projectRepository;
        this.issueRepository = issueRepository;
        this.sprintMapper = sprintMapper;
        this.issueMapper = issueMapper;
    }

    @Override
    public SprintResponseDto create(CreateSprintRequestDto request) {
        ProjectEntity project = findProject(request.projectKey());
        SprintEntity sprint = new SprintEntity();
        sprint.setProject(project);
        sprint.setName(request.name());
        sprint.setGoal(request.goal());
        sprint.setStartDate(request.startDate());
        sprint.setEndDate(request.endDate());
        sprint.setStatus(SprintStatus.PLANNED);
        sprintRepository.save(sprint);
        return toResponse(sprint, List.of());
    }

    @Override
    public SprintResponseDto start(Long id) {
        SprintEntity sprint = findSprint(id);
        if (sprintRepository.existsByProjectIdAndStatus(sprint.getProject().getId(), SprintStatus.ACTIVE)) {
            throw new ApiException(ErrorCode.SPRINT_ALREADY_ACTIVE,
                    "Another sprint is already active for this project");
        }
        sprint.setStatus(SprintStatus.ACTIVE);
        return toResponse(sprint, issueRepository.findBySprintId(sprint.getId()));
    }

    @Override
    public SprintResponseDto complete(Long id) {
        SprintEntity sprint = findSprint(id);
        if (sprint.getStatus() != SprintStatus.ACTIVE) {
            throw new ApiException(ErrorCode.SPRINT_NOT_ACTIVE, "Sprint is not active");
        }
        sprint.setStatus(SprintStatus.COMPLETED);
        // Unfinished issues fall back to the backlog.
        List<IssueEntity> issues = issueRepository.findBySprintId(sprint.getId());
        issues.stream()
                .filter(issue -> issue.getStatus() != IssueStatus.DONE)
                .forEach(issue -> issue.setSprint(null));
        return toResponse(sprint, issueRepository.findBySprintId(sprint.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public BacklogResponseDto backlog(String projectKey) {
        ProjectEntity project = findProject(projectKey);

        List<BacklogSprintDto> sprints = sprintRepository
                .findByProjectIdOrderByStartDateAsc(project.getId()).stream()
                .filter(sprint -> sprint.getStatus() != SprintStatus.COMPLETED)
                .map(sprint -> {
                    List<IssueEntity> issues = issueRepository.findBySprintId(sprint.getId());
                    return new BacklogSprintDto(toResponse(sprint, issues), issueMapper.toCards(issues));
                })
                .toList();

        List<IssueEntity> unscheduled = issueRepository.findByProjectIdAndSprintIsNull(project.getId());
        return new BacklogResponseDto(project.getProjectKey(), sprints, issueMapper.toCards(unscheduled));
    }

    private SprintResponseDto toResponse(SprintEntity sprint, List<IssueEntity> issues) {
        int total = issues.size();
        int done = (int) issues.stream().filter(i -> i.getStatus() == IssueStatus.DONE).count();
        int points = issues.stream()
                .map(IssueEntity::getStoryPoints)
                .filter(p -> p != null)
                .mapToInt(Integer::intValue)
                .sum();
        return sprintMapper.toResponse(sprint, total, done, points);
    }

    private ProjectEntity findProject(String projectKey) {
        return projectRepository.findByProjectKey(projectKey)
                .orElseThrow(() -> new ApiException(ErrorCode.PROJECT_NOT_FOUND,
                        "Project " + projectKey + " not found"));
    }

    private SprintEntity findSprint(Long id) {
        return sprintRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SPRINT_NOT_FOUND, "Sprint " + id + " not found"));
    }
}
