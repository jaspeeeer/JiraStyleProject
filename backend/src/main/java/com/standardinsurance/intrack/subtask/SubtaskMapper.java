package com.standardinsurance.intrack.subtask;

import com.standardinsurance.intrack.subtask.dto.SubtaskResponseDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubtaskMapper {

    @Mapping(target = "assigneeId", source = "assignee.id")
    @Mapping(target = "assigneeName", source = "assignee.name")
    SubtaskResponseDto toResponse(SubtaskEntity entity);

    List<SubtaskResponseDto> toResponses(List<SubtaskEntity> entities);
}
