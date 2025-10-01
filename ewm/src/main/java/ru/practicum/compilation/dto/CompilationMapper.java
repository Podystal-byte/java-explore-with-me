package ru.practicum.compilation.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.mapper.EventMapper;

import java.util.Set;

@Mapper(componentModel = "spring", uses = {EventMapper.class})
public interface CompilationMapper {

    CompilationDto toCompilationDto(Compilation compilation);

    @Mapping(target = "events", source = "eventsSet")
    CompilationDto toCompilationDto(Compilation compilation, Set<EventShortDto> eventsSet);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    Compilation toCompilation(NewCompilationDto newCompilationDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    @Mapping(target = "title", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "pinned", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCompilation(@MappingTarget Compilation compilation, UpdateCompilationRequest updateRequest);
}