package ru.practicum.ewm.service;

import ru.practicum.ewm.grpc.stats.event.RecommendedEventProto;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.util.List;

public interface EventSimilarityService {
    void updateOrCreateSimilarity(EventSimilarityAvro eventSimilarityAvro);

    List<RecommendedEventProto> getSimilarEvents(long userId, long sampleEventId, int limit);

    List<RecommendedEventProto> getRecommendationsForUser(long userId, int limit);
}
