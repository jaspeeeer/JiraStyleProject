package com.standardinsurance.intrack.epic;

import com.standardinsurance.intrack.common.domain.BaseEntity;
import com.standardinsurance.intrack.project.ProjectEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "epics")
@Getter
@Setter
@NoArgsConstructor
public class EpicEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    @Column(name = "epic_key", nullable = false)
    private String epicKey;

    @Column(nullable = false)
    private String name;

    private String color;

    @Column(columnDefinition = "text")
    private String description;

    /** Planned timeframe shown on the roadmap. Both nullable — an epic may be unscheduled. */
    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;
}
