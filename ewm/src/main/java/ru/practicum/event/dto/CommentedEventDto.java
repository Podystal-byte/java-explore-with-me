package ru.practicum.event.dto;

import lombok.Data;

@Data
public class CommentedEventDto {
    private Long id;
    private String title;
    private String annotation;
}