package ru.practicum.comments.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.NewCommentDto;
import ru.practicum.comments.service.CommentService;


@RestController
@RequestMapping("/comments/{userId}")
@RequiredArgsConstructor
@Validated
public class PrivateCommentController {

    private final CommentService commentService;

    @PostMapping("/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@PathVariable Long userId, @PathVariable Long eventId, @RequestBody @Valid NewCommentDto newCommentDto) {

        return commentService.createComment(userId, eventId, newCommentDto);
    }


    @PatchMapping("/{commentId}")
    public CommentDto updateComment(@PathVariable Long userId, @PathVariable Long commentId, @RequestBody @Valid NewCommentDto updatedCommentDto) {

        return commentService.updateComment(userId, commentId, updatedCommentDto);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long userId, @PathVariable Long commentId) {

        commentService.deleteComment(commentId, userId);
    }
}