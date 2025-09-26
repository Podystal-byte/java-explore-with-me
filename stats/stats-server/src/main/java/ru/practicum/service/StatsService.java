package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.StatsDto;
import ru.practicum.models.Hit;
import ru.practicum.repository.HitRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsService {

    private final HitRepository hitRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Transactional
    public HitDto addHit(HitDto hitDto) {
        Hit hit = Hit.builder()
                .app(hitDto.getApp())
                .uri(hitDto.getUri())
                .ip(hitDto.getIp())
                .timestamp(LocalDateTime.parse(hitDto.getTime(), FORMATTER))
                .build();

        hitRepository.save(hit);
        log.info("Hit saved: {}", hit);
        return hitDto;
    }

    @Transactional(readOnly = true)
    public List<StatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        validateDateRange(start, end);

        List<StatsDto> result;
        if (Boolean.TRUE.equals(unique)) {
            result = hitRepository.getUniqueStats(start, end, uris);
        } else {
            result = hitRepository.getStats(start, end, uris);
        }
        log.info("getStats result: {}", result);
        return result;
    }

    private void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Дата начала не может быть позже даты окончания.");
        }
    }
}
