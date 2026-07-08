package com.standardinsurance.intrack.project;

import com.standardinsurance.intrack.project.dto.CreateProjectRequestDto;
import com.standardinsurance.intrack.project.dto.ProjectResponseDto;
import java.util.List;

public interface ProjectService {

    List<ProjectResponseDto> listProjects();

    ProjectResponseDto create(CreateProjectRequestDto request);
}
