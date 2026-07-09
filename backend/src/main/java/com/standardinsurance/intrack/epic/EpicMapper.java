package com.standardinsurance.intrack.epic;

import com.standardinsurance.intrack.epic.dto.EpicResponseDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EpicMapper {

    @Mapping(target = "key", source = "epicKey")
    EpicResponseDto toResponse(EpicEntity entity);

    List<EpicResponseDto> toResponses(List<EpicEntity> entities);
}
