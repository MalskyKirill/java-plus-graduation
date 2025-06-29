package ru.practicum.ewm.storage;

import java.util.Set;

public interface InteractionMatrixStorage {
    double getWeight(long eventId, long userId);

    Set<Long> getEvents(long userId);

    void put(long eventId, long userId, double weight);
}
