package com.standardinsurance.intrack.activity;

import com.standardinsurance.intrack.activity.dto.ActivityResponseDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ActivityMapper {

    @Mapping(target = "actorName", source = "actor.name")
    ActivityResponseDto toResponse(ActivityLogEntity entity);

    List<ActivityResponseDto> toResponses(List<ActivityLogEntity> entities);
}
