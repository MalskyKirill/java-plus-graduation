package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.grpc.stats.event.RecommendedEventProto;
import ru.practicum.ewm.mapper.WeightMapper;
import ru.practicum.ewm.model.Weight;
import ru.practicum.ewm.repository.WeightRepository;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActionServiceImpl implements UserActionService{
    private final WeightRepository weightRepository;
    private final WeightMapper weightMapper;

    public void updateOrCreateWeight(UserActionAvro userActionAvro) {
        Weight newWeight = weightMapper.toWeight(userActionAvro);
        weightRepository.findByEventIdAndUserId(newWeight.getEventId(), newWeight.getUserId())
            .ifPresentOrElse(
                existingWeight -> updateIfGreater(existingWeight, newWeight),
                () -> saveNewWeight(newWeight)
            );
    }

    public List<RecommendedEventProto> getTotalInteractionWeight(List<Long> eventIds) {
        List<Weight> weights = weightRepository.findAllByEventIdIn(eventIds);
        log.debug("Веса: {} для списка eventIds: {}", weights, eventIds);
        Map<Long, Double> totalWeights = weights.stream()
            .collect(Collectors.groupingBy(
                Weight::getEventId,
                Collectors.summingDouble(Weight::getWeight)
            ));
        log.debug("Подсчет общего веса взаимодействия: {}", totalWeights);
        return totalWeights.entrySet().stream()
            .map(entry -> RecommendedEventProto.newBuilder()
                .setEventId(entry.getKey())
                .setScore(entry.getValue())
                .build())
            .collect(Collectors.toList());
    }

    public List<Weight> getByUserIdAndEventIds(long userId, Set<Long> eventIds) {
        List<Weight> weights = weightRepository.findByUserIdAndEventIdIn(userId, eventIds);
        log.debug("Выбранные веса {} для пользователя: {} взаимосвязанные события: {}", weights.size(), userId, weights);
        return weights;
    }

    public Set<Long> getAllEventIdByUserId(long userId) {
        Set<Long> eventIds = weightRepository.findAllEventIdByUserId(userId);
        log.debug("Выбранные {} события для пользователя {}", eventIds.size(), userId);
        return eventIds;
    }

    public List<Long> getLastInteractedEvents(long userId, int limit) {
        List<Long> lastInteracted = weightRepository.findLastInteractedEventIds(userId, limit);
        log.debug("Извлечено {} последнее взаимодействие пользователя: {} события: {}",
            lastInteracted.size(), userId, lastInteracted);
        return lastInteracted;
    }

    private void updateIfGreater(Weight existingWeight, Weight newWeight) {
        if (newWeight.getWeight() > existingWeight.getWeight()) {
            log.info("Обновление веса для userId: {}, eventId: {} от {} до {}",
                existingWeight.getUserId(),
                existingWeight.getEventId(),
                existingWeight.getWeight(),
                newWeight.getWeight());
            existingWeight.setWeight(newWeight.getWeight());
            existingWeight.setTimestamp(newWeight.getTimestamp());
            weightRepository.save(existingWeight);
        } else {
            log.info("Текущий вес: {} для userId: {}, eventId: {} больше или равен новому весу: {}, не обновляем",
                existingWeight.getWeight(),
                existingWeight.getUserId(),
                existingWeight.getEventId(),
                newWeight.getWeight());
        }
    }

    private void saveNewWeight(Weight newWeight) {
        log.info("Сохранение нового веса для userId={}, eventId={}, вес={}",
            newWeight.getUserId(),
            newWeight.getEventId(),
            newWeight.getWeight());
        Weight weight = weightRepository.save(newWeight);
        log.debug("Вес сохранен: {}", weight);
    }
}
