package com.standardinsurance.intrack.project;

import com.standardinsurance.intrack.common.domain.BaseEntity;
import com.standardinsurance.intrack.user.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
public class ProjectEntity extends BaseEntity {

    @Column(name = "project_key", nullable = false, unique = true)
    private String projectKey;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    /** Per-project counter backing human issue keys ({@code PROJ-1}, {@code PROJ-2}, …). */
    @Column(name = "issue_counter", nullable = false)
    private int issueCounter = 0;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "project_members",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<UserEntity> members = new HashSet<>();
}
