package com.standardinsurance.intrack.activity;

import com.standardinsurance.intrack.activity.dto.ActivityResponseDto;
import com.standardinsurance.intrack.common.error.ApiException;
import com.standardinsurance.intrack.common.error.ErrorCode;
import com.standardinsurance.intrack.issue.IssueEntity;
import com.standardinsurance.intrack.issue.IssueRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ActivityServiceImpl implements ActivityService {

    private final ActivityLogRepository activityLogRepository;
    private final IssueRepository issueRepository;
    private final ActivityMapper activityMapper;

    public ActivityServiceImpl(ActivityLogRepository activityLogRepository,
                               IssueRepository issueRepository,
                               ActivityMapper activityMapper) {
        this.activityLogRepository = activityLogRepository;
        this.issueRepository = issueRepository;
        this.activityMapper = activityMapper;
    }

    @Override
    public List<ActivityResponseDto> list(String issueKey) {
        IssueEntity issue = issueRepository.findByIssueKey(issueKey)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Issue " + issueKey + " not found"));
        return activityMapper.toResponses(activityLogRepository.findByIssueIdOrderByCreatedAtDesc(issue.getId()));
    }
}
