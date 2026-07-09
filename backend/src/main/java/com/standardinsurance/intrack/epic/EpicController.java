package com.standardinsurance.intrack.epic;

import com.standardinsurance.intrack.epic.dto.CreateEpicRequestDto;
import com.standardinsurance.intrack.epic.dto.EpicResponseDto;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class EpicController {

    private final EpicService epicService;

    public EpicController(EpicService epicService) {
        this.epicService = epicService;
    }

    @GetMapping("/projects/{projectKey}/epics")
    public List<EpicResponseDto> list(@PathVariable String projectKey) {
        return epicService.list(projectKey);
    }

    @PostMapping("/epics")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','PROJECT_LEAD')")
    public EpicResponseDto create(@Valid @RequestBody CreateEpicRequestDto request) {
        return epicService.create(request);
    }
}
