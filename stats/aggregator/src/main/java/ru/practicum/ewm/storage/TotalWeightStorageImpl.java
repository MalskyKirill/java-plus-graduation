package ru.practicum.ewm.storage;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class TotalWeightStorageImpl implements TotalWeightStorage{
    private final Map<Long, Double> totalEventWeights = new HashMap<>();

    @Override
    public double get(long eventId) {
        return totalEventWeights.getOrDefault(eventId, 0.0);
    }

    @Override
    public void put(long eventId, double weight) {
        totalEventWeights.put(eventId, weight);
    }
}
