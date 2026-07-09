package com.standardinsurance.intrack.roadmap;

import com.standardinsurance.intrack.common.error.ApiException;
import com.standardinsurance.intrack.common.error.ErrorCode;
import com.standardinsurance.intrack.epic.EpicRepository;
import com.standardinsurance.intrack.issue.IssueEntity;
import com.standardinsurance.intrack.issue.IssueRepository;
import com.standardinsurance.intrack.issue.IssueStatus;
import com.standardinsurance.intrack.project.ProjectEntity;
import com.standardinsurance.intrack.project.ProjectRepository;
import com.standardinsurance.intrack.roadmap.dto.RoadmapEpicDto;
import com.standardinsurance.intrack.roadmap.dto.RoadmapResponseDto;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RoadmapServiceImpl implements RoadmapService {

    private final ProjectRepository projectRepository;
    private final EpicRepository epicRepository;
    private final IssueRepository issueRepository;

    public RoadmapServiceImpl(ProjectRepository projectRepository,
                              EpicRepository epicRepository,
                              IssueRepository issueRepository) {
        this.projectRepository = projectRepository;
        this.epicRepository = epicRepository;
        this.issueRepository = issueRepository;
    }

    @Override
    public RoadmapResponseDto get(String projectKey) {
        ProjectEntity project = projectRepository.findByProjectKey(projectKey)
                .orElseThrow(() -> new ApiException(ErrorCode.PROJECT_NOT_FOUND,
                        "Project " + projectKey + " not found"));

        List<RoadmapEpicDto> epics = epicRepository.findByProjectIdOrderByIdAsc(project.getId()).stream()
                .map(epic -> {
                    List<IssueEntity> issues = issueRepository.findByEpicId(epic.getId());
                    int done = (int) issues.stream()
                            .filter(issue -> issue.getStatus() == IssueStatus.DONE)
                            .count();
                    return new RoadmapEpicDto(
                            epic.getId(),
                            epic.getEpicKey(),
                            epic.getName(),
                            epic.getColor(),
                            epic.getStartDate(),
                            epic.getEndDate(),
                            issues.size(),
                            done);
                })
                .toList();

        return new RoadmapResponseDto(project.getProjectKey(), epics);
    }
}
