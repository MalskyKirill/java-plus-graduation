package ru.practicum.ewm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class StatsClient extends BaseClient {

    @Autowired
    public StatsClient(@Value("${stats-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder
            .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
            .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
            .build()
        );
    }

    public ResponseEntity<Object> addHit(EndpointHitInputDto hitDto) {
        log.info("Отправлен Post /hit запрос на сервер с данными {}", hitDto);
        return post("/hit", hitDto);
    }

    public ResponseEntity<Object> getStats(@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                           @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                           List<String> uris,
                                           Boolean unique) {

        Map<String, Object> parameters = new HashMap<>();
        StringBuilder uriBuilder = new StringBuilder("/stats");
        uriBuilder.append("?unique=").append(unique);
        parameters.put("unique", unique);

        if (start != null) {
            uriBuilder.append("&start=").append(start.toString());
            parameters.put("start", start.toString());
        }
        if (end != null) {
            uriBuilder.append("&end=").append(end.toString());
            parameters.put("end", end.toString());
        }
        if (uris != null && !uris.isEmpty()) {
            uriBuilder.append("&uris=").append(String.join(",", uris));
            parameters.put("uris", String.join(",", uris));
        }

        String uri = uriBuilder.toString();
        log.info("Отправлен Get /stats запрос на сервер с данными " + uri);

        ResponseEntity<Object> response = get(uri, parameters);

        log.info("Получен ответ Get /stats с сервера статистики с телом {}", response.getBody());
        return response;
    }
}
