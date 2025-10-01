package ru.practicum.event.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.category.dto.CategoriesMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoriesRepository;
import ru.practicum.controller.StatsClient;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.StatsDto;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.mapper.LocationMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.Location;
import ru.practicum.event.model.State;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.repository.LocationRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.dto.RequestMapper;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.users.dto.UserMapper;
import ru.practicum.users.model.User;
import ru.practicum.users.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoriesRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final RequestRepository requestRepository;
    private final EventMapper eventMapper;
    private final CategoriesMapper categoriesMapper;
    private final UserMapper userMapper;
    private final LocationMapper locationMapper;
    private final StatsClient statsClient;
    private final RequestMapper requestMapper;

    @PersistenceContext
    private EntityManager em;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<EventShortDto> getEventsByUser(Long userId, Integer from, Integer size) {
        User user = getUserById(userId);

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());
        List<Event> events = eventRepository.findByInitiatorId(userId, pageable).getContent();
        return events.stream().map(event -> eventMapper.toShortDto(event, categoriesMapper.toDto(event.getCategory()), userMapper.toShortDto(user))).collect(Collectors.toList());
    }

    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        User user = getUserById(userId);
        Category category = getCategoryById(newEventDto.getCategory());

        LocalDateTime eventDateTime = LocalDateTime.parse(newEventDto.getEventDate(), java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        if (eventDateTime.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Дата события должна быть не ранее чем через 2 часа от текущего момента");
        }

        if (newEventDto.getParticipantLimit() != null && newEventDto.getParticipantLimit() < 0) {
            throw new ValidationException("Лимит участников не может быть отрицательным");
        }

        Location location = locationMapper.toLocation(newEventDto.getLocation());
        Location savedLocation = locationRepository.save(location);

        Event event = eventMapper.toEntity(newEventDto);
        event.setInitiator(user);
        event.setCategory(category);
        event.setLocation(savedLocation);

        Event savedEvent = eventRepository.save(event);
        return eventMapper.toFullDto(savedEvent, categoriesMapper.toDto(category), userMapper.toShortDto(user), locationMapper.toLocationDto(savedLocation));
    }

    public EventFullDto getEventByUser(Long userId, Long eventId) {
        User user = getUserById(userId);
        Event event = getEventById(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Событие не принадлежит пользователю");
        }
        return eventMapper.toFullDto(event, categoriesMapper.toDto(event.getCategory()), userMapper.toShortDto(user), locationMapper.toLocationDto(event.getLocation()));
    }

    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        User user = getUserById(userId);
        Event event = getEventById(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Событие не принадлежит пользователю");
        }

        if (event.getState() != State.PENDING && event.getState() != State.CANCELED) {
            throw new ConflictException("Можно изменять только ожидающие или отмененные события");
        }

        if (updateEventUserRequest.getEventDate() != null) {
            LocalDateTime newEventDate = LocalDateTime.parse(updateEventUserRequest.getEventDate(), java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            if (newEventDate.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ValidationException("Дата события должна быть не ранее чем через 2 часа от текущего момента");
            }
        }

        if (updateEventUserRequest.getParticipantLimit() != null && updateEventUserRequest.getParticipantLimit() < 0) {
            throw new ValidationException("Лимит участников не может быть отрицательным");
        }

        if (updateEventUserRequest.getCategory() != null) {
            Category category = getCategoryById(updateEventUserRequest.getCategory());
            event.setCategory(category);
        }

        if (updateEventUserRequest.getLocation() != null) {
            Location location = locationMapper.toLocation(updateEventUserRequest.getLocation());
            Location savedLocation = locationRepository.save(location);
            event.setLocation(savedLocation);
        }

        eventMapper.updateFromUserRequest(updateEventUserRequest, event);

        if (updateEventUserRequest.getStateAction() != null) {
            switch (updateEventUserRequest.getStateAction()) {
                case "SEND_TO_REVIEW":
                    event.setState(State.PENDING);
                    break;
                case "CANCEL_REVIEW":
                    event.setState(State.CANCELED);
                    break;
            }
        }

        Event updatedEvent = eventRepository.save(event);
        return eventMapper.toFullDto(updatedEvent, categoriesMapper.toDto(updatedEvent.getCategory()), userMapper.toShortDto(updatedEvent.getInitiator()), locationMapper.toLocationDto(updatedEvent.getLocation()));
    }


    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        getUserById(userId);
        Event event = getEventById(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Событие не принадлежит пользователю");
        }

        List<ParticipationRequest> requests = requestRepository.findByEventId(eventId);
        return requests.stream().map(requestMapper::toDto).collect(Collectors.toList());
    }

    public EventRequestStatusUpdateResult updateEventRequests(Long userId, Long eventId, EventRequestStatusUpdateRequest request) {
        if (request == null || request.getRequestIds() == null || request.getRequestIds().isEmpty() || request.getStatus() == null || request.getStatus().isEmpty()) {
            throw new ValidationException("Некорректное тело запроса или отсутствующие данные");
        }

        getUserById(userId);
        Event event = getEventById(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Событие не принадлежит пользователю");
        }

        if ("CONFIRMED".equals(request.getStatus()) && event.getParticipantLimit() > 0 && event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new ConflictException("Достигнут лимит участников");
        }

        List<ParticipationRequest> requests = requestRepository.findAllById(request.getRequestIds());

        if (requests.size() != request.getRequestIds().size()) {
            throw new NotFoundException("Один или несколько запросов не найдены");
        }

        boolean allPending = requests.stream().allMatch(req -> req.getStatus() == RequestStatus.PENDING);
        if (!allPending) {
            throw new ConflictException("Все запросы должны быть в состоянии ожидания");
        }

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult(new ArrayList<>(), new ArrayList<>());
        List<ParticipationRequest> toSave = new ArrayList<>();

        if ("CONFIRMED".equals(request.getStatus())) {
            int availableSlots = event.getParticipantLimit() - event.getConfirmedRequests().intValue();
            int toConfirm = Math.min(availableSlots, requests.size());

            for (int i = 0; i < toConfirm; i++) {
                ParticipationRequest req = requests.get(i);
                req.setStatus(RequestStatus.CONFIRMED);
                toSave.add(req);
                result.getConfirmedRequests().add(requestMapper.toDto(req));
            }

            for (int i = toConfirm; i < requests.size(); i++) {
                ParticipationRequest req = requests.get(i);
                req.setStatus(RequestStatus.REJECTED);
                toSave.add(req);
                result.getRejectedRequests().add(requestMapper.toDto(req));
            }

            requestRepository.saveAll(toSave);

            event.setConfirmedRequests(event.getConfirmedRequests() + toConfirm);
            eventRepository.save(event);

        } else if ("REJECTED".equals(request.getStatus())) {
            for (ParticipationRequest req : requests) {
                req.setStatus(RequestStatus.REJECTED);
                toSave.add(req);
                result.getRejectedRequests().add(requestMapper.toDto(req));
            }
            requestRepository.saveAll(toSave);
        }

        return result;
    }

    public List<EventFullDto> getEventsByAdmin(List<Long> users, List<State> states, List<Long> categories, LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());

        if (rangeStart == null) rangeStart = LocalDateTime.now();
        if (rangeEnd == null) rangeEnd = LocalDateTime.now().plusYears(100);
        users = users == null ? Collections.emptyList() : users;
        List<String> strStates = states == null ? Collections.emptyList() : states.stream().map(State::name).toList();

        categories = categories == null ? Collections.emptyList() : categories;

        List<Event> events = eventRepository.findEventsForAdmin(users, strStates, categories, rangeStart, rangeEnd, pageable).getContent();
        return events.stream().map(event -> eventMapper.toFullDto(event, categoriesMapper.toDto(event.getCategory()), userMapper.toShortDto(event.getInitiator()), locationMapper.toLocationDto(event.getLocation()))).collect(Collectors.toList());
    }

    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event event = getEventById(eventId);

        if (updateEventAdminRequest.getEventDate() != null) {
            LocalDateTime newEventDate = LocalDateTime.parse(updateEventAdminRequest.getEventDate(), java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            if (newEventDate.isBefore(LocalDateTime.now().plusHours(1))) {
                throw new ValidationException("Дата начала события должна быть не ранее чем за час от публикации");
            }
        }

        if (updateEventAdminRequest.getCategory() != null) {
            Category category = getCategoryById(updateEventAdminRequest.getCategory());
            event.setCategory(category);
        }

        if (updateEventAdminRequest.getLocation() != null) {
            Location location = locationMapper.toLocation(updateEventAdminRequest.getLocation());
            Location savedLocation = locationRepository.save(location);
            event.setLocation(savedLocation);
        }

        eventMapper.updateFromAdminRequest(updateEventAdminRequest, event);

        if (updateEventAdminRequest.getStateAction() != null) {
            switch (updateEventAdminRequest.getStateAction()) {
                case "PUBLISH_EVENT":
                    if (event.getState() != State.PENDING) {
                        throw new ConflictException("Можно публиковать только события в состоянии ожидания");
                    }
                    event.setState(State.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                case "REJECT_EVENT":
                    if (event.getState() == State.PUBLISHED) {
                        throw new ConflictException("Нельзя отклонить опубликованное событие");
                    }
                    event.setState(State.CANCELED);
                    break;
            }
        }

        Event updatedEvent = eventRepository.save(event);
        return eventMapper.toFullDto(updatedEvent, categoriesMapper.toDto(updatedEvent.getCategory()), userMapper.toShortDto(updatedEvent.getInitiator()), locationMapper.toLocationDto(updatedEvent.getLocation()));
    }

    private void saveHitStatistic(String endpoint, String clientIp) {
        String timestampString = LocalDateTime.now().format(formatter);

        HitDto hit = new HitDto("ewm-main-service", endpoint, clientIp, timestampString);
        statsClient.addHit(hit);
    }

    public List<EventShortDto> getEventsPublic(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, Integer from, Integer size, String clientIp, String endpoint) {

        saveHitStatistic(endpoint, clientIp);

        Pageable pageable = PageRequest.of(from / size, size);

        if (rangeStart == null) rangeStart = LocalDateTime.now();
        if (rangeEnd == null) rangeEnd = LocalDateTime.now().plusYears(100);

        if (categories != null) {
            categories = categories.stream().filter(id -> id > 0).collect(Collectors.toList());
        } else {
            categories = Collections.emptyList();
        }

        if (rangeEnd.isBefore(rangeStart)) {
            throw new ValidationException("rangeEnd не может быть раньше rangeStart");
        }


        List<Event> events = eventRepository.findPublicEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, pageable).getContent();

        updateViewsForEvents(events);


        if ("EVENT_DATE".equals(sort)) {
            List<Event> sortedEvents = new ArrayList<>(events);
            sortedEvents.sort((e1, e2) -> e2.getEventDate().compareTo(e1.getEventDate()));
            events = sortedEvents;
        } else if ("VIEWS".equals(sort)) {
            List<Event> sortedEvents = new ArrayList<>(events);
            sortedEvents.sort((e1, e2) -> e2.getViews().compareTo(e1.getViews()));
            events = sortedEvents;
        }

        return events.stream().map(event -> eventMapper.toShortDto(event, categoriesMapper.toDto(event.getCategory()), userMapper.toShortDto(event.getInitiator()))).collect(Collectors.toList());
    }

    public EventFullDto getEventPublic(Long id, String clientIp, String endpoint) {
        Event event = getPublishEventById(id);

        updateViewsForSingleEvent(event);
        saveHitStatistic(endpoint, clientIp);

        return eventMapper.toFullDto(event, categoriesMapper.toDto(event.getCategory()), userMapper.toShortDto(event.getInitiator()), locationMapper.toLocationDto(event.getLocation()));
    }

    private void updateViewsForEvents(List<Event> events) {

        if (events.isEmpty()) return;

        try {
            LocalDateTime start = LocalDateTime.now().minusYears(100);
            LocalDateTime end = LocalDateTime.now();

            List<String> uris = events.stream().map(event -> "/events/" + event.getId()).collect(Collectors.toList());

            List<StatsDto> stats = statsClient.getStats(start, end, uris, false);

            Map<Long, Long> viewsMap = new HashMap<>();
            for (StatsDto stat : stats) {
                try {
                    Long eventId = Long.parseLong(stat.getUri().substring("/events/".length()));
                    viewsMap.put(eventId, stat.getHits());
                } catch (NumberFormatException e) {
                    log.warn("Невозможно распарсить ID события из URI: {}", stat.getUri());
                }
            }

            for (Event event : events) {
                Long currentViews = viewsMap.getOrDefault(event.getId(), event.getViews());
                event.setViews(currentViews);
            }

        } catch (Exception e) {
            log.error("Ошибка при пакетном обновлении просмотров: {}", e.getMessage());
        }
    }

    private void updateViewsForSingleEvent(Event event) {
        try {
            LocalDateTime start = LocalDateTime.now().minusYears(100);
            LocalDateTime end = LocalDateTime.now();
            String eventUri = "/events/" + event.getId();

            List<StatsDto> stats = statsClient.getStats(start, end, List.of(eventUri), true);

            if (!stats.isEmpty()) {
                event.setViews((long) stats.size());
            }

        } catch (Exception e) {
            log.error("Ошибка при обновлении просмотров для события {}: {}", event.getId(), e.getMessage());
        }
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
    }

    private Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(() -> new NotFoundException("Категория с ID " + categoryId + " не найдена"));
    }

    private Event getEventById(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено"));
    }

    private Event getPublishEventById(Long eventId) {
        return eventRepository.findPublishedById(eventId).orElseThrow(() -> new NotFoundException("Опубликованное событие с ID " + eventId + " не найдено"));
    }
}
