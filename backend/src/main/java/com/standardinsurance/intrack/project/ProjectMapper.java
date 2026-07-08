package com.standardinsurance.intrack.project;

import com.standardinsurance.intrack.project.dto.ProjectResponseDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(target = "key", source = "projectKey")
    ProjectResponseDto toResponse(ProjectEntity entity);

    List<ProjectResponseDto> toResponseList(List<ProjectEntity> entities);
}
