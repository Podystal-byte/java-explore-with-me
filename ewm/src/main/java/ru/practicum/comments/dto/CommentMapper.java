package ru.practicum.comments.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.practicum.comments.dto.CommentDto;

import ru.practicum.comments.dto.NewCommentDto;
import ru.practicum.comments.model.Comment;
import ru.practicum.event.dto.CommentedEventDto;
import ru.practicum.event.model.Event;
import ru.practicum.users.dto.UserShortDto;
import ru.practicum.users.model.User;

import java.util.List;

@Mapper(componentModel = "spring") // Указываем Spring для интеграции с IoC контейнером
public interface CommentMapper {

    CommentMapper INSTANCE = Mappers.getMapper(CommentMapper.class);

    Comment toComment(NewCommentDto newCommentDto);

    @Mapping(target = "author", source = "author")
    @Mapping(target = "event", source = "event")
    CommentDto toCommentDto(Comment comment);

    UserShortDto toUserShortDto(User user);

    CommentedEventDto toCommentedEventDto(Event event);

    List<CommentDto> toCommentDtoList(List<Comment> comments);
}