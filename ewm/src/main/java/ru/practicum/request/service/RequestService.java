package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.event.model.Event;

import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.dto.RequestMapper;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.users.model.User;
import ru.practicum.users.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.request.model.RequestStatus.CONFIRMED;

@Service
@RequiredArgsConstructor
public class RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final RequestMapper requestMapper;
    private final EventRepository eventRepository;

    public List<ParticipationRequestDto> getRequests(Long userId) {
        getUser(userId);
        List<ParticipationRequest> requests = requestRepository.findByRequesterId(userId);
        return requests.stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    public ParticipationRequestDto create(Long userId, Long eventId) {
        User user = getUser(userId);
        Event event = getPublishedEvent(eventId);

        checkNoDuplicate(userId, eventId);
        checkNotInitiator(userId, event);

        checkLimit(event, eventId);
        RequestStatus status = resolveStatus(event);

        ParticipationRequest request = requestMapper.toRequest(event, user, status);
        return requestMapper.toDto(requestRepository.save(request));
    }

    public ParticipationRequestDto cancel(Long userId, Long requestId) {
        getUser(userId);
        ParticipationRequest request = getRequest(requestId);

        if (!request.getRequester().getId().equals(userId)) {
            throw new NotFoundException("Запрос не этого пользователя.");
        }

        request.setStatus(RequestStatus.CANCELED);
        ParticipationRequest canceledRequest = requestRepository.save(request);

        if (request.getStatus() == CONFIRMED) {
            Event event = request.getEvent();
            event.setConfirmedRequests(event.getConfirmedRequests() - 1);
            eventRepository.save(event);
        }

        return requestMapper.toDto(canceledRequest);
    }

    private ParticipationRequest getRequest(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос id " + requestId + " не найден"));
    }

    private Event getPublishedEvent(Long eventId) {
        return eventRepository.findPublishedById(eventId)
                .orElseThrow(() -> new ConflictException ("Событие не опубликовано."));
    }


    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь id " + userId + " не найден"));
    }

    private void checkNoDuplicate(Long userId, Long eventId) {
        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException("Заявка уже отправлена.");
        }
    }

    private void checkNotInitiator(Long userId, Event event) {
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Инициатор не может подавать заявку на свое событие.");
        }
    }

    private void checkLimit(Event event, Long eventId) {
        long confirmed = requestRepository.countByEventIdAndStatus(eventId, CONFIRMED);
        if (event.getParticipantLimit() > 0 && confirmed >= event.getParticipantLimit()) {
            throw new ConflictException("Достигнут лимит участников.");
        }
    }

    private RequestStatus resolveStatus(Event event) {
        boolean autoConfirm = !Boolean.TRUE.equals(event.getRequestModeration())
                || event.getParticipantLimit() == 0;
        if (autoConfirm) {
            return CONFIRMED;
        } else {
            return RequestStatus.PENDING;
        }
    }
}
