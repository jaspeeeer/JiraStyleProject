package com.standardinsurance.intrack.issue;

import org.springframework.data.jpa.domain.Specification;

/** Reusable board/issue filters. */
public final class IssueSpecifications {

    private IssueSpecifications() {
    }

    public static Specification<IssueEntity> inProject(Long projectId) {
        return (root, query, cb) -> cb.equal(root.get("project").get("id"), projectId);
    }

    public static Specification<IssueEntity> hasAssignee(Long assigneeId) {
        return (root, query, cb) -> cb.equal(root.get("assignee").get("id"), assigneeId);
    }

    public static Specification<IssueEntity> hasPriority(Priority priority) {
        return (root, query, cb) -> cb.equal(root.get("priority"), priority);
    }

    public static Specification<IssueEntity> hasType(IssueType type) {
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    public static Specification<IssueEntity> hasEpic(Long epicId) {
        return (root, query, cb) -> cb.equal(root.get("epic").get("id"), epicId);
    }
}
