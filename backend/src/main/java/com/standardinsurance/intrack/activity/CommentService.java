package com.standardinsurance.intrack.activity;

import com.standardinsurance.intrack.activity.dto.CommentResponseDto;
import com.standardinsurance.intrack.activity.dto.CreateCommentRequestDto;
import java.util.List;

public interface CommentService {

    List<CommentResponseDto> list(String issueKey);

    CommentResponseDto create(String issueKey, CreateCommentRequestDto request);
}
