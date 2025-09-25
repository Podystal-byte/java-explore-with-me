package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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


    public HitDto addHit(HitDto hitDto) {
        Hit hit = Hit.builder()
                .app(hitDto.getApp())
                .uri(hitDto.getUri())
                .ip(hitDto.getIp())
                .timestamps(LocalDateTime.parse(hitDto.getTime(), FORMATTER))
                .build();
        hitRepository.save(hit);
        return hitDto;
    }

    public List<StatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (uris == null || uris.isEmpty()) {
            if (unique) {
                return hitRepository.getUniqueStats(start, end);
            } else {
                return hitRepository.getStats(start, end);
            }
        } else {
            if (unique) {
                return hitRepository.getUniqueStatsWithUris(start, end, uris);
            } else {
                return hitRepository.getStatsWithUris(start, end, uris);
            }
        }
    }
}
