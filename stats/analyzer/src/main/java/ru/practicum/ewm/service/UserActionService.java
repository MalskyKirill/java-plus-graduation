package ru.practicum.ewm.service;

import ru.practicum.ewm.grpc.stats.event.RecommendedEventProto;
import ru.practicum.ewm.model.Weight;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.List;
import java.util.Set;

public interface UserActionService {
    void updateOrCreateWeight(UserActionAvro userActionAvro);

    List<RecommendedEventProto> getTotalInteractionWeight(List<Long> eventIds);

    List<Weight> getByUserIdAndEventIds(long userId, Set<Long> eventIds);

    Set<Long> getAllEventIdByUserId(long userId);

    List<Long> getLastInteractedEvents(long userId, int limit);
}
