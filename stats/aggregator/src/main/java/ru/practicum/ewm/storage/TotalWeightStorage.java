package ru.practicum.ewm.storage;

public interface TotalWeightStorage {
    double get(long eventId);

    void put(long eventId, double weight);
}
