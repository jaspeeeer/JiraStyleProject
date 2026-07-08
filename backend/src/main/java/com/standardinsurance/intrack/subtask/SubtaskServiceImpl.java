package com.standardinsurance.intrack.subtask;

import com.standardinsurance.intrack.common.error.ApiException;
import com.standardinsurance.intrack.common.error.ErrorCode;
import com.standardinsurance.intrack.issue.IssueEntity;
import com.standardinsurance.intrack.issue.IssueRepository;
import com.standardinsurance.intrack.subtask.dto.CreateSubtaskRequestDto;
import com.standardinsurance.intrack.subtask.dto.SubtaskResponseDto;
import com.standardinsurance.intrack.subtask.dto.UpdateSubtaskRequestDto;
import com.standardinsurance.intrack.user.UserEntity;
import com.standardinsurance.intrack.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SubtaskServiceImpl implements SubtaskService {

    private final SubtaskRepository subtaskRepository;
    private final IssueRepository issueRepository;
    private final UserRepository userRepository;
    private final SubtaskMapper subtaskMapper;

    public SubtaskServiceImpl(SubtaskRepository subtaskRepository,
                              IssueRepository issueRepository,
                              UserRepository userRepository,
                              SubtaskMapper subtaskMapper) {
        this.subtaskRepository = subtaskRepository;
        this.issueRepository = issueRepository;
        this.userRepository = userRepository;
        this.subtaskMapper = subtaskMapper;
    }

    @Override
    public SubtaskResponseDto create(String issueKey, CreateSubtaskRequestDto request) {
        IssueEntity issue = issueRepository.findByIssueKey(issueKey)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Issue " + issueKey + " not found"));

        SubtaskEntity subtask = new SubtaskEntity();
        subtask.setIssue(issue);
        subtask.setTitle(request.title());
        subtask.setDone(false);
        subtask.setOrderIndex((int) subtaskRepository.countByIssueId(issue.getId()));
        if (request.assigneeId() != null) {
            subtask.setAssignee(findUser(request.assigneeId()));
        }
        subtaskRepository.save(subtask);
        return subtaskMapper.toResponse(subtask);
    }

    @Override
    public SubtaskResponseDto update(Long id, UpdateSubtaskRequestDto request) {
        SubtaskEntity subtask = subtaskRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Subtask " + id + " not found"));
        if (request.title() != null) {
            subtask.setTitle(request.title());
        }
        if (request.done() != null) {
            subtask.setDone(request.done());
        }
        if (request.assigneeId() != null) {
            subtask.setAssignee(findUser(request.assigneeId()));
        }
        return subtaskMapper.toResponse(subtask);
    }

    @Override
    public void delete(Long id) {
        if (!subtaskRepository.existsById(id)) {
            throw new ApiException(ErrorCode.NOT_FOUND, "Subtask " + id + " not found");
        }
        subtaskRepository.deleteById(id);
    }

    private UserEntity findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User " + id + " not found"));
    }
}
