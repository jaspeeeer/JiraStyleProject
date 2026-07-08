package com.standardinsurance.intrack.activity;

import com.standardinsurance.intrack.activity.dto.CommentResponseDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorName", source = "author.name")
    CommentResponseDto toResponse(CommentEntity entity);

    List<CommentResponseDto> toResponses(List<CommentEntity> entities);
}
