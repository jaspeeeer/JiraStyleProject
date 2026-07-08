package com.standardinsurance.intrack.activity;

import com.standardinsurance.intrack.common.domain.BaseEntity;
import com.standardinsurance.intrack.issue.IssueEntity;
import com.standardinsurance.intrack.user.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * System-generated audit entry for an issue (field changes, transitions). Append-only;
 * kept separate from user-authored {@link CommentEntity}.
 */
@Entity
@Table(name = "activity_logs")
@Getter
@Setter
@NoArgsConstructor
public class ActivityLogEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "issue_id", nullable = false)
    private IssueEntity issue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private UserEntity actor;

    @Column(nullable = false)
    private String action;

    private String field;

    @Column(name = "old_value", columnDefinition = "text")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "text")
    private String newValue;
}
