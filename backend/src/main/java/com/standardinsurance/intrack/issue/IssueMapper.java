package com.standardinsurance.intrack.issue;

import com.standardinsurance.intrack.issue.dto.IssueCardDto;
import com.standardinsurance.intrack.issue.dto.IssueResponseDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IssueMapper {

    @Mapping(target = "key", source = "issueKey")
    @Mapping(target = "projectKey", source = "project.projectKey")
    @Mapping(target = "assigneeId", source = "assignee.id")
    @Mapping(target = "assigneeName", source = "assignee.name")
    @Mapping(target = "reporterId", source = "reporter.id")
    @Mapping(target = "reporterName", source = "reporter.name")
    @Mapping(target = "epicId", source = "epic.id")
    @Mapping(target = "epicName", source = "epic.name")
    @Mapping(target = "sprintId", source = "sprint.id")
    @Mapping(target = "sprintName", source = "sprint.name")
    IssueResponseDto toResponse(IssueEntity entity);

    @Mapping(target = "key", source = "issueKey")
    @Mapping(target = "assigneeId", source = "assignee.id")
    @Mapping(target = "assigneeName", source = "assignee.name")
    @Mapping(target = "epicName", source = "epic.name")
    IssueCardDto toCard(IssueEntity entity);

    List<IssueCardDto> toCards(List<IssueEntity> entities);
}
