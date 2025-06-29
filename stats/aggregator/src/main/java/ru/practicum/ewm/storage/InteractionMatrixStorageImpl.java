package ru.practicum.ewm.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Repository
public class InteractionMatrixStorageImpl implements InteractionMatrixStorage{
    private final Map<Long, Map<Long, Double>> interactionMatrix = new HashMap<>();

    @Override
    public double getWeight(long eventId, long userId) {
        Map<Long, Double> userMap = interactionMatrix.get(userId);
        if (userMap != null && userMap.containsKey(eventId)) {
            double weight = userMap.get(eventId);
            log.debug("Найден вес userId: {}, eventId: {}, weight: {}", userId, eventId, weight);
            return weight;
        }
        log.debug("Не найден вес для userId: {}, eventId: {}", userId, eventId);
        return 0.0;
    }

    @Override
    public Set<Long> getEvents(long userId) {
        return interactionMatrix.getOrDefault(userId, Collections.emptyMap()).keySet();
    }


    @Override
    public void put(long eventId, long userId, double weight) {
        interactionMatrix
            .computeIfAbsent(userId, key -> {
                log.debug("Создана матрица взаимодействия для нового пользователя: {},", userId);
                return new HashMap<>();
            }).put(eventId, weight);
    }
}
