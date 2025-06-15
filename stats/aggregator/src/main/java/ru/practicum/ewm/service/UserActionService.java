package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.handler.AggregatorSimilarityHandler;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.storage.InteractionMatrixStorage;
import ru.practicum.ewm.storage.MinWeightSumStorage;
import ru.practicum.ewm.storage.TotalWeightStorage;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActionService {
    private final InteractionMatrixStorage interactionMatrixStorage;
    private final TotalWeightStorage totalWeightStorage;
    private final MinWeightSumStorage minWeightSumStorage;
    private final AggregatorSimilarityHandler aggregatorSimilarityHandler;

    public void process(UserActionAvro userAction) {
        long userId = userAction.getUserId();
        long eventId = userAction.getEventId();
        double oldWeight = getCurrentWeight(eventId, userId);
        double newWeight = getReceivedWeight(userAction);
        if (newWeight <= oldWeight) {
            log.debug("Новый вес: {} не больше старого веса: {}. Пропуск обновления.", newWeight, oldWeight);
            return;
        }
        updateInteractionWeight(eventId, userId, newWeight);
        double newTotalEventWeight = updateTotalEventWeight(eventId, oldWeight, newWeight);
        Set<Long> otherEventsInteractedByUser = getOtherEventsInteractedByUser(userId, eventId);
        for (Long otherEventId : otherEventsInteractedByUser) {
            double minWeightSum = updateMinWeightSum(eventId, otherEventId, userId, oldWeight, newWeight);
            double otherEventWeight = totalWeightStorage.get(otherEventId);
            float similarity = (float) calculateEventSimilarity(minWeightSum, newTotalEventWeight, otherEventWeight);
            aggregatorSimilarityHandler.sendSimilarity(eventId, otherEventId, similarity);
        }
    }

    private double getReceivedWeight(UserActionAvro userAction) {
        double weight = switch (userAction.getActionType()) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
        log.debug("Полученный вес взаимодействия: {}. userId: {}, eventId: {}, actionType: {}",
            weight, userAction.getUserId(), userAction.getEventId(), userAction.getActionType());
        return weight;
    }

    private double getCurrentWeight(long eventId, long userId) {
        return interactionMatrixStorage.getWeight(eventId, userId);
    }

    private void updateInteractionWeight(long eventId, long userId, double newWeight) {
        interactionMatrixStorage.put(eventId, userId, newWeight);
    }

    private Set<Long> getOtherEventsInteractedByUser(long userId, long eventId) {
        return interactionMatrixStorage.getEvents(userId).stream()
            .filter(otherEventId -> otherEventId != eventId)
            .collect(Collectors.toSet());
    }

    private double updateTotalEventWeight(long eventId, double currentWeight, double newWeight) {
        double oldTotalWeight = totalWeightStorage.get(eventId);
        double delta = newWeight - currentWeight;
        double newTotalWeight = oldTotalWeight + delta;
        totalWeightStorage.put(eventId, newTotalWeight);
        return newTotalWeight;
    }

    private double updateMinWeightSum(long eventId, long otherEventId, long userId, double oldWeight, double newWeight) {
        double otherEventWeight = interactionMatrixStorage.getWeight(otherEventId, userId);
        double oldMin = Math.min(oldWeight, otherEventWeight);
        double minNew = Math.min(newWeight, otherEventWeight);
        double delta = minNew - oldMin;
        double oldMinSum = minWeightSumStorage.get(eventId, otherEventId);
        if (delta != 0) {
            double newMinSum = oldMinSum + delta;
            minWeightSumStorage.put(eventId, otherEventId, newMinSum);
            log.debug("Обновленная минимальная весовая сумма для пары событий ({}, {}): дельта = {}", eventId, otherEventId, delta);
            return newMinSum;
        } else {
            log.debug("Минимальный вес для пары событий не изменился ({}, {})", eventId, otherEventId);
            return oldMinSum;
        }
    }

    private double calculateEventSimilarity(
        double minWeightSum,
        double totalEventWeight,
        double totalOtherEventWeight) {
        double denominator = Math.sqrt(totalEventWeight) * Math.sqrt(totalOtherEventWeight);
        return minWeightSum / denominator;
    }
}
