package ru.practicum.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class HitDto {
    @NotBlank
    private String app;
    @NotBlank
    private String uri;
    @NotBlank
    private String ip;
    @NotBlank
    private String time;
}
