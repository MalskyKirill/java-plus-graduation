package ru.practicum.ewm.storage;

public interface MinWeightSumStorage {
    double get(long eventAId, long eventBId);

    void put(long eventAId, long eventBId, double sum);
}
