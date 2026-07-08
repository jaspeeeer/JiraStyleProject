package com.standardinsurance.intrack.subtask;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.standardinsurance.intrack.issue.IssueEntity;
import com.standardinsurance.intrack.issue.IssueRepository;
import com.standardinsurance.intrack.subtask.dto.CreateSubtaskRequestDto;
import com.standardinsurance.intrack.subtask.dto.UpdateSubtaskRequestDto;
import com.standardinsurance.intrack.user.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubtaskServiceImplTest {

    @Mock SubtaskRepository subtaskRepository;
    @Mock IssueRepository issueRepository;
    @Mock UserRepository userRepository;
    @Mock SubtaskMapper subtaskMapper;

    @InjectMocks SubtaskServiceImpl subtaskService;

    @Captor ArgumentCaptor<SubtaskEntity> subtaskCaptor;

    @Test
    void createAppendsWithNextOrderIndexAndNotDone() {
        IssueEntity issue = new IssueEntity();
        issue.setId(1L);
        given(issueRepository.findByIssueKey("PROJ-1")).willReturn(Optional.of(issue));
        given(subtaskRepository.countByIssueId(1L)).willReturn(2L);

        subtaskService.create("PROJ-1", new CreateSubtaskRequestDto("Write tests", null));

        verify(subtaskRepository).save(subtaskCaptor.capture());
        assertThat(subtaskCaptor.getValue().getTitle()).isEqualTo("Write tests");
        assertThat(subtaskCaptor.getValue().getOrderIndex()).isEqualTo(2);
        assertThat(subtaskCaptor.getValue().isDone()).isFalse();
    }

    @Test
    void updateTogglesDone() {
        SubtaskEntity subtask = new SubtaskEntity();
        subtask.setDone(false);
        given(subtaskRepository.findById(5L)).willReturn(Optional.of(subtask));

        subtaskService.update(5L, new UpdateSubtaskRequestDto(null, true, null));

        assertThat(subtask.isDone()).isTrue();
    }
}
