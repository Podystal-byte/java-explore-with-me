package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.CompilationMapper;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.category.dto.CategoriesMapper;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.users.dto.UserMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompilationService  {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;
    private final EventMapper eventMapper;
    private final CategoriesMapper categoriesMapper;
    private final UserMapper userMapper;

    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        if (compilationRepository.existsByTitle(newCompilationDto.getTitle())) {
            throw new ValidationException("Подборка с названием '" + newCompilationDto.getTitle() + "' уже существует");
        }

        Compilation compilation = compilationMapper.toCompilation(newCompilationDto);

        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            Set<Event> events = new HashSet<>(eventRepository.findAllById(newCompilationDto.getEvents()));
            compilation.setEvents(events);
        }

        Compilation savedCompilation = compilationRepository.save(compilation);

        return convertToDtoWithEvents(savedCompilation);
    }

    public void deleteCompilation(Long compId) {
        Compilation compilation = getCompilationIfExists(compId);
        compilationRepository.delete(compilation);
    }

    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateRequest) {
        Compilation compilation = getCompilationIfExists(compId);

        if (updateRequest.getTitle() != null &&
                compilationRepository.existsByTitleAndIdNot(updateRequest.getTitle(), compId)) {
            throw new ValidationException("Подборка с названием '" + updateRequest.getTitle() + "' уже существует");
        }

        if (updateRequest.getTitle() != null) {
            compilation.setTitle(updateRequest.getTitle());
        }
        if (updateRequest.getPinned() != null) {
            compilation.setPinned(updateRequest.getPinned());
        }
        if (updateRequest.getEvents() != null) {
            Set<Event> events = new HashSet<>(eventRepository.findAllById(updateRequest.getEvents()));
            compilation.setEvents(events);
        }

        Compilation updatedCompilation = compilationRepository.save(compilation);

        return convertToDtoWithEvents(updatedCompilation);
    }

    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        PageRequest page = PageRequest.of(from / size, size);

        List<Compilation> compilations;
        if (pinned != null) {
            compilations = compilationRepository.findAllByPinned(pinned, page).getContent();
        } else {
            compilations = compilationRepository.findAll(page).getContent();
        }

        return compilations.stream()
                .map(this::convertToDtoWithEvents)
                .collect(Collectors.toList());
    }

    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = getCompilationIfExists(compId);
        return convertToDtoWithEvents(compilation);
    }

    private CompilationDto convertToDtoWithEvents(Compilation compilation) {
        Set<EventShortDto> eventDtos = compilation.getEvents().stream()
                .map(event -> eventMapper.toShortDto(
                        event,
                        categoriesMapper.toDto(event.getCategory()),
                        userMapper.toShortDto(event.getInitiator())
                ))
                .collect(Collectors.toSet());

        return compilationMapper.toCompilationDto(compilation, eventDtos);
    }

    private Compilation getCompilationIfExists(Long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id=" + compId + " не найдена"));
    }
}