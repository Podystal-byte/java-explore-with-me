package ru.practicum.comments.dto;

import lombok.Data;
import ru.practicum.event.dto.CommentedEventDto;
import ru.practicum.users.dto.UserShortDto;

import java.time.LocalDateTime;

@Data
public class CommentDto {

    private Long id;

    private String description;

    private UserShortDto author;

    private CommentedEventDto event;

    private LocalDateTime createdOn;
}