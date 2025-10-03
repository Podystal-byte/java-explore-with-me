package ru.practicum.event.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationDto {
    @NotNull(message = "Latitude must not be null")
    private Float lat;

    @NotNull(message = "Longitude must not be null")
    private Float lon;
}