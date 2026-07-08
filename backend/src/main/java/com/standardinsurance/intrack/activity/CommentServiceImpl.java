package com.standardinsurance.intrack.activity;

import com.standardinsurance.intrack.activity.dto.CommentResponseDto;
import com.standardinsurance.intrack.activity.dto.CreateCommentRequestDto;
import com.standardinsurance.intrack.common.error.ApiException;
import com.standardinsurance.intrack.common.error.ErrorCode;
import com.standardinsurance.intrack.common.security.CurrentUser;
import com.standardinsurance.intrack.issue.IssueEntity;
import com.standardinsurance.intrack.issue.IssueRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final IssueRepository issueRepository;
    private final CommentMapper commentMapper;
    private final CurrentUser currentUser;

    public CommentServiceImpl(CommentRepository commentRepository,
                              IssueRepository issueRepository,
                              CommentMapper commentMapper,
                              CurrentUser currentUser) {
        this.commentRepository = commentRepository;
        this.issueRepository = issueRepository;
        this.commentMapper = commentMapper;
        this.currentUser = currentUser;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponseDto> list(String issueKey) {
        IssueEntity issue = findIssue(issueKey);
        return commentMapper.toResponses(commentRepository.findByIssueIdOrderByCreatedAtAsc(issue.getId()));
    }

    @Override
    public CommentResponseDto create(String issueKey, CreateCommentRequestDto request) {
        IssueEntity issue = findIssue(issueKey);
        CommentEntity comment = new CommentEntity();
        comment.setIssue(issue);
        comment.setAuthor(currentUser.require());
        comment.setBody(request.body());
        commentRepository.save(comment);
        return commentMapper.toResponse(comment);
    }

    private IssueEntity findIssue(String issueKey) {
        return issueRepository.findByIssueKey(issueKey)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Issue " + issueKey + " not found"));
    }
}
