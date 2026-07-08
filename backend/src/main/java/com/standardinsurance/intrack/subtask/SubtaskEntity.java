package com.standardinsurance.intrack.subtask;

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

@Entity
@Table(name = "subtasks")
@Getter
@Setter
@NoArgsConstructor
public class SubtaskEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "issue_id", nullable = false)
    private IssueEntity issue;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private boolean done = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private UserEntity assignee;

    @Column(name = "order_index", nullable = false)
    private int orderIndex = 0;
}
