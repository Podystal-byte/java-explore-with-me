package ru.practicum.compilation.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.service.CompilationService;


import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/compilations")
public class PublicCompilationsController {
    private final CompilationService compilationService;

    @GetMapping
    public List<CompilationDto> getCompilations(@RequestParam(required = false) Boolean pinned, @RequestParam(defaultValue = "0") @PositiveOrZero Integer from, @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Получаем список подборок: закрепленные={}, пропустить={}, размер={}", pinned, from, size);
        return compilationService.getCompilations(pinned, from, size);
    }

    @GetMapping("/{compId}")
    public CompilationDto getCompilationById(@PathVariable Long compId) {
        log.info("Ищем подборку id={}", compId);
        return compilationService.getCompilationById(compId);
    }

}