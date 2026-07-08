package com.standardinsurance.intrack.user;

import com.standardinsurance.intrack.common.domain.BaseEntity;
import com.standardinsurance.intrack.project.ProjectEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class UserEntity extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(name = "last_active_at")
    private Instant lastActiveAt;

    /** Inverse side of the project ↔ user membership (owned by {@code ProjectEntity#members}). */
    @ManyToMany(mappedBy = "members", fetch = FetchType.LAZY)
    private Set<ProjectEntity> projects = new HashSet<>();
}
