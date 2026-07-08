package com.standardinsurance.intrack.subtask;

import com.standardinsurance.intrack.subtask.dto.CreateSubtaskRequestDto;
import com.standardinsurance.intrack.subtask.dto.SubtaskResponseDto;
import com.standardinsurance.intrack.subtask.dto.UpdateSubtaskRequestDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class SubtaskController {

    private final SubtaskService subtaskService;

    public SubtaskController(SubtaskService subtaskService) {
        this.subtaskService = subtaskService;
    }

    @PostMapping("/issues/{key}/subtasks")
    @ResponseStatus(HttpStatus.CREATED)
    public SubtaskResponseDto create(@PathVariable String key,
                                     @Valid @RequestBody CreateSubtaskRequestDto request) {
        return subtaskService.create(key, request);
    }

    @PatchMapping("/subtasks/{id}")
    public SubtaskResponseDto update(@PathVariable Long id,
                                     @Valid @RequestBody UpdateSubtaskRequestDto request) {
        return subtaskService.update(id, request);
    }

    @DeleteMapping("/subtasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        subtaskService.delete(id);
    }
}
