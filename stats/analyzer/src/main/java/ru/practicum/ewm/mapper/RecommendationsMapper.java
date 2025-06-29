package ru.practicum.ewm.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.grpc.stats.event.RecommendedEventProto;
import ru.practicum.ewm.model.RecommendedEvent;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecommendationsMapper {
    List<RecommendedEventProto> mapToProto(List<RecommendedEvent> recommendedEvents);

    RecommendedEventProto mapToProto(RecommendedEvent recommendedEvent);
}
