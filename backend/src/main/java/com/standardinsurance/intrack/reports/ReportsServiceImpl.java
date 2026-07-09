package com.standardinsurance.intrack.reports;

import com.standardinsurance.intrack.common.error.ApiException;
import com.standardinsurance.intrack.common.error.ErrorCode;
import com.standardinsurance.intrack.issue.IssueEntity;
import com.standardinsurance.intrack.issue.IssueRepository;
import com.standardinsurance.intrack.issue.IssueStatus;
import com.standardinsurance.intrack.issue.IssueType;
import com.standardinsurance.intrack.issue.Priority;
import com.standardinsurance.intrack.project.ProjectEntity;
import com.standardinsurance.intrack.project.ProjectRepository;
import com.standardinsurance.intrack.reports.dto.CountDto;
import com.standardinsurance.intrack.reports.dto.ReportsResponseDto;
import com.standardinsurance.intrack.reports.dto.VelocityPointDto;
import com.standardinsurance.intrack.sprint.SprintRepository;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReportsServiceImpl implements ReportsService {

    private final ProjectRepository projectRepository;
    private final IssueRepository issueRepository;
    private final SprintRepository sprintRepository;

    public ReportsServiceImpl(ProjectRepository projectRepository,
                              IssueRepository issueRepository,
                              SprintRepository sprintRepository) {
        this.projectRepository = projectRepository;
        this.issueRepository = issueRepository;
        this.sprintRepository = sprintRepository;
    }

    @Override
    public ReportsResponseDto get(String projectKey) {
        ProjectEntity project = projectRepository.findByProjectKey(projectKey)
                .orElseThrow(() -> new ApiException(ErrorCode.PROJECT_NOT_FOUND,
                        "Project " + projectKey + " not found"));

        List<IssueEntity> issues = issueRepository.findByProjectIdOrderByIdAsc(project.getId());

        long doneIssues = issues.stream().filter(ReportsServiceImpl::isDone).count();
        long totalPoints = sumPoints(issues);
        long donePoints = sumPoints(issues.stream().filter(ReportsServiceImpl::isDone).toList());

        List<CountDto> statusCounts = countBy(issues, Arrays.stream(IssueStatus.values()).map(Enum::name).toList(),
                issue -> issue.getStatus().name());
        List<CountDto> priorityCounts = countBy(issues, Arrays.stream(Priority.values()).map(Enum::name).toList(),
                issue -> issue.getPriority().name());
        List<CountDto> typeCounts = countBy(issues, Arrays.stream(IssueType.values()).map(Enum::name).toList(),
                issue -> issue.getType().name());

        List<VelocityPointDto> velocity = sprintRepository
                .findByProjectIdOrderByStartDateAsc(project.getId()).stream()
                .map(sprint -> {
                    List<IssueEntity> sprintIssues = issueRepository.findBySprintId(sprint.getId());
                    int completed = (int) sumPoints(
                            sprintIssues.stream().filter(ReportsServiceImpl::isDone).toList());
                    return new VelocityPointDto(sprint.getId(), sprint.getName(), sprint.getStatus(),
                            completed, (int) sumPoints(sprintIssues));
                })
                .toList();

        return new ReportsResponseDto(project.getProjectKey(), issues.size(), doneIssues,
                totalPoints, donePoints, statusCounts, priorityCounts, typeCounts, velocity);
    }

    private static boolean isDone(IssueEntity issue) {
        return issue.getStatus() == IssueStatus.DONE;
    }

    private static long sumPoints(List<IssueEntity> issues) {
        return issues.stream()
                .map(IssueEntity::getStoryPoints)
                .filter(points -> points != null)
                .mapToLong(Integer::longValue)
                .sum();
    }

    /** Counts issues per bucket, emitting every bucket (zeros included) in the given order. */
    private static List<CountDto> countBy(List<IssueEntity> issues, List<String> buckets,
                                          Function<IssueEntity, String> classifier) {
        return buckets.stream()
                .map(bucket -> new CountDto(bucket,
                        issues.stream().filter(issue -> bucket.equals(classifier.apply(issue))).count()))
                .toList();
    }
}
