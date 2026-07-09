package com.standardinsurance.intrack.epic;

import com.standardinsurance.intrack.common.error.ApiException;
import com.standardinsurance.intrack.common.error.ErrorCode;
import com.standardinsurance.intrack.epic.dto.CreateEpicRequestDto;
import com.standardinsurance.intrack.epic.dto.EpicResponseDto;
import com.standardinsurance.intrack.project.ProjectEntity;
import com.standardinsurance.intrack.project.ProjectRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EpicServiceImpl implements EpicService {

    private final EpicRepository epicRepository;
    private final ProjectRepository projectRepository;
    private final EpicMapper epicMapper;

    public EpicServiceImpl(EpicRepository epicRepository,
                           ProjectRepository projectRepository,
                           EpicMapper epicMapper) {
        this.epicRepository = epicRepository;
        this.projectRepository = projectRepository;
        this.epicMapper = epicMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EpicResponseDto> list(String projectKey) {
        ProjectEntity project = findProject(projectKey);
        return epicMapper.toResponses(epicRepository.findByProjectIdOrderByIdAsc(project.getId()));
    }

    @Override
    public EpicResponseDto create(CreateEpicRequestDto request) {
        ProjectEntity project = findProject(request.projectKey());

        EpicEntity epic = new EpicEntity();
        epic.setProject(project);
        // Epics are never deleted, so a count-based sequence stays unique per project.
        epic.setEpicKey("E" + (epicRepository.countByProjectId(project.getId()) + 1));
        epic.setName(request.name());
        epic.setColor(request.color());
        epic.setDescription(request.description());
        epic.setStartDate(request.startDate());
        epic.setEndDate(request.endDate());
        epicRepository.save(epic);
        return epicMapper.toResponse(epic);
    }

    private ProjectEntity findProject(String projectKey) {
        return projectRepository.findByProjectKey(projectKey)
                .orElseThrow(() -> new ApiException(ErrorCode.PROJECT_NOT_FOUND,
                        "Project " + projectKey + " not found"));
    }
}
