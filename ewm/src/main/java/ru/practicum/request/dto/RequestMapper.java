package ru.practicum.request.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.event.model.Event;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.users.model.User;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring", imports = {LocalDateTime.class})
public interface RequestMapper {

    @Mapping(target = "event", source = "event")
    @Mapping(target = "requester", source = "user")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "created", expression = "java(LocalDateTime.now())")
    @Mapping(target = "id", ignore = true)
    ParticipationRequest toRequest(Event event, User user, RequestStatus status);

    @Mapping(target = "event", source = "event.id")
    @Mapping(target = "requester", source = "requester.id")
    @Mapping(target = "status", source = "status")
    ParticipationRequestDto toDto(ParticipationRequest request);
}
