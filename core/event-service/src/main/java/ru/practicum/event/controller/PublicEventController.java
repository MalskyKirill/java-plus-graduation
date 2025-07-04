package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.EventSort;
import ru.practicum.event.service.PublicEventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PublicEventController {
    private final PublicEventService publicEventService;

    @GetMapping("/{id}")
    public EventFullDto getEventById(@PathVariable long id,
                                     @RequestHeader("X-EWM-USER-ID") Long userId,
                                     HttpServletRequest request) {
        log.info("Поступил запрос Get /events/{} на получение Event с id = {}", id, id);
        EventFullDto response = publicEventService.getEventById(id, userId, request);
        log.info("Сформирован ответ Get /events/{} с телом: {}", id, response);
        return response;
    }

    @GetMapping
    public List<EventShortDto> getEvents(@RequestParam(required = false) String text,
                                         @RequestParam(required = false) List<Long> categories,
                                         @RequestParam(required = false) Boolean paid,
                                         @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                         @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                         @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                         @RequestParam(required = false) EventSort sorts,
                                         @RequestParam(defaultValue = "0") @PositiveOrZero(message = "Параметр 'from' не может быть отрицательным") int from,
                                         @RequestParam(defaultValue = "10") @Positive(message = "Параметр 'size' должен быть больше 0") int size,
                                         HttpServletRequest request) {
        log.info("Поступил запрос Get /events на получение Events с text = {}, size = {}", text, size);
        return publicEventService.getEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sorts, from, size, request);
    }

    @GetMapping("/recommendations")
    public List<EventShortDto> getRecommendations(@RequestHeader("X-EWM-USER-ID") Long userId,
                                                  @RequestParam Integer maxResults) {
        log.info("Поступил запрос Get /events/recommendations на получение рекомендаций Events для пользователя с id = {}, кол-во событий = {}", userId, maxResults);
        List<EventShortDto> response = publicEventService.getRecommendations(userId, maxResults);
        log.info("Сформирован ответ Get /events/recommendations с телом: {}", response);
        return response;

    }

    @PutMapping("/{eventId}/like")
    public void likeEvent(@PathVariable Long eventId,
                          @RequestHeader("X-EWM-USER-ID") Long userId) {
        log.info("Поступил запрос Put /events/{}/like на лайк события с id = {} пользователем с id = {}", eventId, eventId, userId);
        publicEventService.likeEvent(eventId, userId);
        log.info("Обработан запрос Put /events/{}/like на лайк события с id = {} пользователем с id = {}", eventId, eventId, userId);
    }

}
