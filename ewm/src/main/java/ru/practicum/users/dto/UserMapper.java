package ru.practicum.users.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.users.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(User user);

    UserShortDto toShortDto(User user);

    @Mapping(target = "id", ignore = true)
    User toUser(NewUserRequest request);

    List<UserDto> toDtoList(List<User> users);
}
