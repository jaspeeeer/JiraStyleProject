package com.standardinsurance.intrack.epic;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EpicRepository extends JpaRepository<EpicEntity, Long> {

    List<EpicEntity> findByProjectIdOrderByIdAsc(Long projectId);

    long countByProjectId(Long projectId);
}
