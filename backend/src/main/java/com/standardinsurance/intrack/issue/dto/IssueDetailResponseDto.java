package com.standardinsurance.intrack.issue.dto;

import com.standardinsurance.intrack.activity.dto.ActivityResponseDto;
import com.standardinsurance.intrack.activity.dto.CommentResponseDto;
import com.standardinsurance.intrack.subtask.dto.SubtaskResponseDto;
import java.util.List;

/**
 * Aggregate view for the issue detail page: the issue plus its subtasks (with progress),
 * comments, and activity feed.
 */
public record IssueDetailResponseDto(
        IssueResponseDto issue,
        int subtasksDone,
        int subtasksTotal,
        List<SubtaskResponseDto> subtasks,
        List<CommentResponseDto> comments,
        List<ActivityResponseDto> activity
) {
}
