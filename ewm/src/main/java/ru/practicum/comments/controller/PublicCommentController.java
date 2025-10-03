package ru.practicum.comments.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class PublicCommentController {

    private final CommentService commentService;

    @GetMapping("/{commentId}")
    public CommentDto getCommentById(@PathVariable Long commentId) {
        return commentService.getCommentById(commentId);
    }

    @GetMapping("/events/{eventId}")
    public List<CommentDto> getCommentsByEvent(@PathVariable Long eventId, @RequestParam(defaultValue = "0") @PositiveOrZero int from, @RequestParam(defaultValue = "10") @Positive int size) {

        return commentService.getCommentsByEvent(eventId, from, size);
    }
}