package com.standardinsurance.intrack.sprint;

import com.standardinsurance.intrack.sprint.dto.SprintResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SprintMapper {

    @Mapping(target = "totalIssues", source = "totalIssues")
    @Mapping(target = "doneIssues", source = "doneIssues")
    @Mapping(target = "totalPoints", source = "totalPoints")
    SprintResponseDto toResponse(SprintEntity entity, int totalIssues, int doneIssues, int totalPoints);
}
