package com.standardinsurance.intrack.epic;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EpicRepository extends JpaRepository<EpicEntity, Long> {
}
