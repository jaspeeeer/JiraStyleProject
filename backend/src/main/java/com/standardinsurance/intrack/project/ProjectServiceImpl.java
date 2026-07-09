package com.standardinsurance.intrack.project;

import com.standardinsurance.intrack.common.error.ApiException;
import com.standardinsurance.intrack.common.error.ErrorCode;
import com.standardinsurance.intrack.project.dto.CreateProjectRequestDto;
import com.standardinsurance.intrack.project.dto.ProjectResponseDto;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    public ProjectServiceImpl(ProjectRepository projectRepository, ProjectMapper projectMapper) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
    }

    @Override
    public List<ProjectResponseDto> listProjects() {
        // Sorted by id so the list (and the frontend's default project) is deterministic.
        return projectMapper.toResponseList(projectRepository.findAll(Sort.by("id")));
    }

    @Override
    @Transactional
    public ProjectResponseDto create(CreateProjectRequestDto request) {
        if (projectRepository.existsByProjectKey(request.key())) {
            throw new ApiException(ErrorCode.PROJECT_KEY_TAKEN,
                    "Project key " + request.key() + " is already in use");
        }
        ProjectEntity project = new ProjectEntity();
        project.setProjectKey(request.key());
        project.setName(request.name());
        project.setDescription(request.description());
        projectRepository.save(project);
        return projectMapper.toResponse(project);
    }
}
