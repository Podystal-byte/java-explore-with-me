package ru.practicum.comments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewCommentDto {
    @NotBlank(message = "Текст комментария не может быть пустым.")
    @Size(min = 1, max = 2000, message = "Текст должен быть от 1 до 2000 символов.")
    private String description;
}