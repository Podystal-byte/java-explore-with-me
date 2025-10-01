package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewEventDto {
    @NotBlank(message = "Annotation must not be blank")
    @Size(min = 20, max = 2000, message = "Annotation must be between 20 and 2000 characters")
    private String annotation;

    @NotNull(message = "Category must not be null")
    private Long category;

    @NotBlank(message = "Description must not be blank")
    @Size(min = 20, max = 7000, message = "Description must be between 20 and 7000 characters")
    private String description;

    @NotBlank(message = "Event date must not be blank")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String eventDate;

    @NotNull(message = "Location must not be null")
    @Valid
    private LocationDto location;

    private Boolean paid;

    @PositiveOrZero(message = "Лимит участников не может быть отрицательным")
    private Integer participantLimit;

    private Boolean requestModeration;

    @NotBlank(message = "Title must not be blank")
    @Size(min = 3, max = 120, message = "Title must be between 3 and 120 characters")
    private String title;
}