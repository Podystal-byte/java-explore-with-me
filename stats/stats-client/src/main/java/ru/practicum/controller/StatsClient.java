package ru.practicum.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.StatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class StatsClient {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RestTemplate restTemplate;

    @Autowired
    public StatsClient(@Value("${stats-server.url}") String serverUrl, RestTemplateBuilder builder) {
        this.restTemplate = builder.rootUri(serverUrl).build();
    }

    public void addHit(HitDto hitDto) {
        restTemplate.postForLocation("/hit", hitDto);
    }

    public List<StatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {

        StringBuilder uriBuilder = new StringBuilder("/stats?start={start}&end={end}&unique={unique}");
        Map<String, Object> params = Map.of(
                "start", start.format(FORMATTER),
                "end", end.format(FORMATTER),
                "unique", unique
        );

        if (uris != null && !uris.isEmpty()) {
            for (String uri : uris) {
                uriBuilder.append("&uris=").append(uri);
            }
        }

        ResponseEntity<StatsDto[]> response = restTemplate.getForEntity(uriBuilder.toString(), StatsDto[].class, params);
        StatsDto[] statsArray = response.getBody();
        if (statsArray == null) {
            return List.of();
        }
        return Arrays.asList(statsArray);
    }
}
