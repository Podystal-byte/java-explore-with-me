package ru.practicum.users.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.users.dto.NewUserRequest;
import ru.practicum.users.dto.UserDto;
import ru.practicum.users.dto.UserMapper;
import ru.practicum.users.model.User;
import ru.practicum.users.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserDto createUser(NewUserRequest newUserRequest) {
        if (userRepository.existsByEmail(newUserRequest.getEmail())) {
            throw new ConflictException("Пользователь с email " + newUserRequest.getEmail() + " уже существует");
        }
        if (newUserRequest.getEmail().length() > 254 || newUserRequest.getEmail().length() < 6) {
            throw new ValidationException("Неправильный Email");
        }

        if (newUserRequest.getName().length() > 250 || newUserRequest.getName().length() < 2) {
            throw new ValidationException("Неправильный name");
        }

        User user = userMapper.toUser(newUserRequest);
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        if (from < 0 || size <= 0) {
            throw new IllegalArgumentException("Параметры 'from' и 'size' должны быть положительными числами.");
        }

        Pageable pageable = PageRequest.of(from / size, size);
        List<User> users;

        if (ids == null || ids.isEmpty()) {
            Page<User> userPage = userRepository.findAll(pageable);
            users = userPage.getContent();
        } else {
            users = userRepository.findByIdIn(ids, pageable);
        }
        return userMapper.toDtoList(users);
    }

    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        userRepository.deleteById(userId);
    }
}

