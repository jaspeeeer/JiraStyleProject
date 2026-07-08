package com.standardinsurance.intrack.activity;

import com.standardinsurance.intrack.activity.dto.CommentResponseDto;
import com.standardinsurance.intrack.activity.dto.CreateCommentRequestDto;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/issues/{key}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public List<CommentResponseDto> list(@PathVariable String key) {
        return commentService.list(key);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponseDto create(@PathVariable String key,
                                     @Valid @RequestBody CreateCommentRequestDto request) {
        return commentService.create(key, request);
    }
}
