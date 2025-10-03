package ru.practicum.comments.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.CommentMapper;
import ru.practicum.comments.dto.NewCommentDto;
import ru.practicum.comments.model.Comment;
import ru.practicum.comments.repository.CommentRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.users.model.User;
import ru.practicum.users.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public CommentDto createComment(Long userId, Long eventId, NewCommentDto newCommentDto) {
        User author = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден."));

        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено."));

        Comment comment = commentMapper.toComment(newCommentDto);
        comment.setAuthor(author);
        comment.setEvent(event);
        comment.setCreatedOn(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toCommentDto(savedComment);
    }

    public CommentDto updateComment(Long userId, Long commentId, NewCommentDto updatedCommentDto) {
        Comment commentToUpdate = commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException("Комментарий с id=" + commentId + " не найден."));

        if (!commentToUpdate.getAuthor().getId().equals(userId)) {
            throw new ConflictException("Пользователь с id=" + userId + " не является автором комментария и не может его изменить.");
        }

        commentToUpdate.setDescription(updatedCommentDto.getDescription());

        Comment savedComment = commentRepository.save(commentToUpdate);
        return commentMapper.toCommentDto(savedComment);
    }

    public CommentDto getCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException("Комментарий с id=" + commentId + " не найден."));

        return commentMapper.toCommentDto(comment);
    }

    public List<CommentDto> getCommentsByEvent(Long eventId, int from, int size) {
        eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено."));

        int page = from / size;
        PageRequest pageRequest = PageRequest.of(page, size);

        List<Comment> comments = commentRepository.findAllByEventId(eventId, pageRequest);

        return commentMapper.toCommentDtoList(comments);
    }


    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException("Комментарий с id=" + commentId + " не найден."));


        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("Пользователь с id=" + userId + " не является автором комментария и не может его удалить.");
        }

        commentRepository.delete(comment);
    }
}