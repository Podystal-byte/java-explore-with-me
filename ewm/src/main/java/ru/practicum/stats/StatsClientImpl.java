package ru.practicum.stats;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.boot.web.client.RestTemplateBuilder;
import ru.practicum.controller.StatsClient;


@Component
public class StatsClientImpl extends StatsClient {

    @Autowired
    public StatsClientImpl(@Value("${stats.server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(serverUrl, builder);
    }
}
