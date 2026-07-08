package com.standardinsurance.intrack.label;

import com.standardinsurance.intrack.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "labels")
@Getter
@Setter
@NoArgsConstructor
public class LabelEntity extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    private String color;
}
