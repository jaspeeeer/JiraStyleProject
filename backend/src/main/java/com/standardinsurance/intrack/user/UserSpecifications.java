package com.standardinsurance.intrack.user;

import org.springframework.data.jpa.domain.Specification;

/**
 * Reusable JPA specifications for filtering the users table. Callers add only the filters that
 * are present (see {@link UserServiceImpl}).
 */
public final class UserSpecifications {

    private UserSpecifications() {
    }

    public static Specification<UserEntity> hasRole(Role role) {
        return (root, query, cb) -> cb.equal(root.get("role"), role);
    }

    public static Specification<UserEntity> hasStatus(UserStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<UserEntity> memberOfProject(Long projectId) {
        return (root, query, cb) -> {
            query.distinct(true);
            return cb.equal(root.join("projects").get("id"), projectId);
        };
    }

    public static Specification<UserEntity> matches(String text) {
        return (root, query, cb) -> {
            String like = "%" + text.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("email")), like));
        };
    }
}
