package com.standardinsurance.intrack.issue;

import com.standardinsurance.intrack.activity.ActivityLogEntity;
import com.standardinsurance.intrack.activity.ActivityLogRepository;
import com.standardinsurance.intrack.common.error.ApiException;
import com.standardinsurance.intrack.common.error.ErrorCode;
import com.standardinsurance.intrack.epic.EpicEntity;
import com.standardinsurance.intrack.epic.EpicRepository;
import com.standardinsurance.intrack.issue.dto.BoardColumnDto;
import com.standardinsurance.intrack.issue.dto.BoardResponseDto;
import com.standardinsurance.intrack.issue.dto.CreateIssueRequestDto;
import com.standardinsurance.intrack.issue.dto.IssueResponseDto;
import com.standardinsurance.intrack.issue.dto.UpdateIssueRequestDto;
import com.standardinsurance.intrack.project.ProjectEntity;
import com.standardinsurance.intrack.project.ProjectRepository;
import com.standardinsurance.intrack.sprint.SprintEntity;
import com.standardinsurance.intrack.sprint.SprintRepository;
import com.standardinsurance.intrack.user.UserEntity;
import com.standardinsurance.intrack.user.UserRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class IssueServiceImpl implements IssueService {

    private final IssueRepository issueRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final EpicRepository epicRepository;
    private final SprintRepository sprintRepository;
    private final ActivityLogRepository activityLogRepository;
    private final IssueMapper issueMapper;

    public IssueServiceImpl(IssueRepository issueRepository,
                            ProjectRepository projectRepository,
                            UserRepository userRepository,
                            EpicRepository epicRepository,
                            SprintRepository sprintRepository,
                            ActivityLogRepository activityLogRepository,
                            IssueMapper issueMapper) {
        this.issueRepository = issueRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.epicRepository = epicRepository;
        this.sprintRepository = sprintRepository;
        this.activityLogRepository = activityLogRepository;
        this.issueMapper = issueMapper;
    }

    @Override
    public IssueResponseDto create(CreateIssueRequestDto request) {
        ProjectEntity project = projectRepository.findByProjectKey(request.projectKey())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND,
                        "Project " + request.projectKey() + " not found"));

        int next = project.getIssueCounter() + 1;
        project.setIssueCounter(next);

        IssueEntity issue = new IssueEntity();
        issue.setProject(project);
        issue.setIssueKey(project.getProjectKey() + "-" + next);
        issue.setTitle(request.title());
        issue.setDescription(request.description());
        issue.setType(request.type());
        issue.setPriority(request.priority());
        issue.setStatus(request.status() != null ? request.status() : IssueStatus.TODO);
        issue.setStoryPoints(request.storyPoints());
        issue.setDueDate(request.dueDate());
        issue.setReporter(currentActor());
        if (request.assigneeId() != null) {
            issue.setAssignee(findUser(request.assigneeId()));
        }
        if (request.epicId() != null) {
            issue.setEpic(findEpic(request.epicId()));
        }
        if (request.sprintId() != null) {
            issue.setSprint(findSprint(request.sprintId()));
        }

        issueRepository.save(issue);
        log(issue, "CREATED", null, null, null);
        return issueMapper.toResponse(issue);
    }

    @Override
    @Transactional(readOnly = true)
    public IssueResponseDto get(String key) {
        return issueMapper.toResponse(findIssue(key));
    }

    @Override
    public IssueResponseDto update(String key, UpdateIssueRequestDto request) {
        IssueEntity issue = findIssue(key);

        if (request.title() != null) {
            issue.setTitle(request.title());
        }
        if (request.description() != null) {
            issue.setDescription(request.description());
        }
        if (request.status() != null && request.status() != issue.getStatus()) {
            log(issue, "STATUS_CHANGED", "status", issue.getStatus().name(), request.status().name());
            issue.setStatus(request.status());
        }
        if (request.priority() != null && request.priority() != issue.getPriority()) {
            log(issue, "PRIORITY_CHANGED", "priority", issue.getPriority().name(), request.priority().name());
            issue.setPriority(request.priority());
        }
        if (request.type() != null) {
            issue.setType(request.type());
        }
        if (request.storyPoints() != null) {
            issue.setStoryPoints(request.storyPoints());
        }
        if (request.dueDate() != null) {
            issue.setDueDate(request.dueDate());
        }
        if (request.assigneeId() != null) {
            UserEntity assignee = findUser(request.assigneeId());
            String old = issue.getAssignee() != null ? issue.getAssignee().getName() : null;
            log(issue, "ASSIGNEE_CHANGED", "assignee", old, assignee.getName());
            issue.setAssignee(assignee);
        }
        if (request.epicId() != null) {
            issue.setEpic(findEpic(request.epicId()));
        }
        if (request.sprintId() != null) {
            issue.setSprint(findSprint(request.sprintId()));
        }

        return issueMapper.toResponse(issue);
    }

    @Override
    @Transactional(readOnly = true)
    public BoardResponseDto board(String projectKey, Long assigneeId, Priority priority,
                                  Long epicId, IssueType type) {
        ProjectEntity project = projectRepository.findByProjectKey(projectKey)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND,
                        "Project " + projectKey + " not found"));

        Specification<IssueEntity> spec = Specification.where(IssueSpecifications.inProject(project.getId()));
        if (assigneeId != null) {
            spec = spec.and(IssueSpecifications.hasAssignee(assigneeId));
        }
        if (priority != null) {
            spec = spec.and(IssueSpecifications.hasPriority(priority));
        }
        if (epicId != null) {
            spec = spec.and(IssueSpecifications.hasEpic(epicId));
        }
        if (type != null) {
            spec = spec.and(IssueSpecifications.hasType(type));
        }

        Map<IssueStatus, List<IssueEntity>> byStatus = issueRepository.findAll(spec).stream()
                .collect(Collectors.groupingBy(IssueEntity::getStatus));

        List<BoardColumnDto> columns = Arrays.stream(IssueStatus.values())
                .map(status -> {
                    List<IssueEntity> issues = byStatus.getOrDefault(status, List.of());
                    return new BoardColumnDto(status, issues.size(), issueMapper.toCards(issues));
                })
                .toList();

        return new BoardResponseDto(project.getProjectKey(), columns);
    }

    private void log(IssueEntity issue, String action, String field, String oldValue, String newValue) {
        ActivityLogEntity entry = new ActivityLogEntity();
        entry.setIssue(issue);
        entry.setActor(currentActor());
        entry.setAction(action);
        entry.setField(field);
        entry.setOldValue(oldValue);
        entry.setNewValue(newValue);
        activityLogRepository.save(entry);
    }

    private UserEntity currentActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return null;
        }
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }

    private IssueEntity findIssue(String key) {
        return issueRepository.findByIssueKey(key)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Issue " + key + " not found"));
    }

    private UserEntity findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User " + id + " not found"));
    }

    private EpicEntity findEpic(Long id) {
        return epicRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Epic " + id + " not found"));
    }

    private SprintEntity findSprint(Long id) {
        return sprintRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Sprint " + id + " not found"));
    }
}
