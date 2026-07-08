package com.standardinsurance.intrack.sprint;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SprintRepository extends JpaRepository<SprintEntity, Long> {

    List<SprintEntity> findByProjectIdOrderByStartDateAsc(Long projectId);

    boolean existsByProjectIdAndStatus(Long projectId, SprintStatus status);
}
