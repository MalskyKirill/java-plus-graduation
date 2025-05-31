package ru.practicum.client;

import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.event.model.Event;

import java.util.Optional;

@FeignClient(name = "event-service", path = "/internal/api/events", contextId = "eventServiceClient")
public interface EventClient {
    @GetMapping("/{id}/full")
    Optional<Event> getEventFullById(@PathVariable long id) throws FeignException;
}
