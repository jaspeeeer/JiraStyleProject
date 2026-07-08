package com.standardinsurance.intrack.project;

import static org.assertj.core.api.Assertions.assertThat;

import com.standardinsurance.intrack.project.dto.ProjectResponseDto;
import org.junit.jupiter.api.Test;

/**
 * Unit test for the MapStruct mapper (no Spring context, no Docker). Uses the generated
 * {@code ProjectMapperImpl} directly.
 */
class ProjectMapperTest {

    private final ProjectMapper mapper = new ProjectMapperImpl();

    @Test
    void mapsProjectKeyToKey() {
        ProjectEntity entity = new ProjectEntity();
        entity.setId(7L);
        entity.setProjectKey("PROJ");
        entity.setName("Platform");
        entity.setDescription("core");
        entity.setIssueCounter(5);

        ProjectResponseDto dto = mapper.toResponse(entity);

        assertThat(dto.id()).isEqualTo(7L);
        assertThat(dto.key()).isEqualTo("PROJ");
        assertThat(dto.name()).isEqualTo("Platform");
        assertThat(dto.description()).isEqualTo("core");
        assertThat(dto.issueCounter()).isEqualTo(5);
    }
}
