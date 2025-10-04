package ru.practicum.comments.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.comments.model.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByAuthorId(Long authorId);

    List<Comment> findAllByEventId(Long eventId, Pageable pageable);

    Optional<Comment> findByIdAndAuthorId(Long commentId, Long authorId);
}