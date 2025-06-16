package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.grpc.stats.event.RecommendedEventProto;
import ru.practicum.ewm.mapper.RecommendationsMapper;
import ru.practicum.ewm.mapper.SimilarityMapper;
import ru.practicum.ewm.model.RecommendedEvent;
import ru.practicum.ewm.model.Similarity;
import ru.practicum.ewm.model.Weight;
import ru.practicum.ewm.repository.SimilarityRepository;
import ru.practicum.ewm.repository.WeightRepository;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventSimilarityServiceImpl implements EventSimilarityService{
    private static final int NEIGHBOR_QUANTITY = 5;

    private final SimilarityMapper similarityMapper;
    private final RecommendationsMapper recommendationsMapper;
    private final SimilarityRepository similarityRepository;
    private final UserActionService userActionService;
    private final WeightRepository weightRepository;

    @Transactional
    public void updateOrCreateSimilarity(EventSimilarityAvro eventSimilarityAvro) {
        Similarity received = similarityMapper.mapToSimilarity(eventSimilarityAvro);
        similarityRepository.findByEventAIdAndEventBId(received.getEventAId(), received.getEventBId()
        ).ifPresentOrElse(
            old -> updateSimilarity(old, received),
            () -> saveNewSimilarity(received)
        );
    }

    private void updateSimilarity(Similarity old, Similarity received) {
        log.info("Обновление сходства: event-A id: {}, event-B id: {}, старая оценка: {}, новая оценка: {}",
            old.getEventAId(), old.getEventBId(), old.getScore(), received.getScore());
        old.setScore(received.getScore());
        old.setTimestamp(received.getTimestamp());
        similarityRepository.save(old);
    }

    private void saveNewSimilarity(Similarity received) {
        log.info("Сохранение нового сходства: eventAId: {}, eventBId: {}, оценка: {}",
            received.getEventAId(), received.getEventBId(), received.getScore());
        similarityRepository.save(received);
    }

    public List<RecommendedEventProto> getSimilarEvents(long userId, long sampleEventId, int limit) {
        List<Similarity> similarities = similarityRepository.findAllByEventId(sampleEventId);
        log.debug("Извлеченные {} сходств для eventId: {}", similarities.size(), sampleEventId);

        Set<Long> interactedEventIds = userActionService.getAllEventIdByUserId(userId);

        List<RecommendedEvent> recommendedEvents = similarities.stream()
            .map(similarity -> {
                // Определяем похожее событие
                long otherEventId = similarity.getEventAId() == sampleEventId
                    ? similarity.getEventBId()
                    : similarity.getEventAId();
                return new RecommendedEvent(otherEventId, similarity.getScore());
            })
            .filter(re -> !interactedEventIds.contains(re.eventId()))
            .limit(limit)
            .toList();
        log.debug("Поиск рекомендуемых событий для пользователя: {}, аналогично событию: {} - {}",
            userId, sampleEventId, recommendedEvents);

        return recommendationsMapper.mapToProto(recommendedEvents);
    }


    public List<RecommendedEventProto> getRecommendationsForUser(long userId, int limit) {
        Set<Long> interactedEventIds = weightRepository.findAllEventIdByUserId(userId);
        if (interactedEventIds.isEmpty()) {
            log.debug("Не найдено взаимодействующих событий для пользователя {}", userId);
            return Collections.emptyList();
        }

        List<Long> lastInteracted = new ArrayList<>(interactedEventIds);
        Collections.reverse(lastInteracted); // если нужно взять последние N
        List<Long> topLastInteracted = lastInteracted.subList(0, Math.min(limit, lastInteracted.size()));

        List<Similarity> allSimilarities = similarityRepository.findSimilaritiesForRecommendation(
            interactedEventIds,
            new HashSet<>(topLastInteracted)
        );

        Set<Long> recommendedCandidates = allSimilarities.stream()
            .flatMap(s -> Stream.of(s.getEventAId(), s.getEventBId()))
            .filter(id -> !interactedEventIds.contains(id))
            .distinct()
            .limit(limit)
            .collect(Collectors.toSet());

        Set<Long> allRelevantEventIds = new HashSet<>(recommendedCandidates);
        allRelevantEventIds.addAll(interactedEventIds);

        List<Weight> neighborWeights = weightRepository.findByUserIdAndEventIdIn(userId, allRelevantEventIds);
        Map<Long, Double> weightsMap = neighborWeights.stream()
            .collect(Collectors.toMap(Weight::getEventId, Weight::getWeight));

        Map<Long, List<Neighbor>> candidateNeighborsMap = buildCandidateNeighborsMap(
            allSimilarities,
            recommendedCandidates,
            weightsMap
        );

        List<RecommendedEvent> recommendedEvents = new ArrayList<>();
        for (Map.Entry<Long, List<Neighbor>> entry : candidateNeighborsMap.entrySet()) {
            long candidateId = entry.getKey();

            List<Neighbor> neighbors = entry.getValue().stream()
                .sorted(Comparator.comparingDouble(Neighbor::similarity).reversed())
                .limit(NEIGHBOR_QUANTITY)
                .toList();

            double score = calculateScore(neighbors);
            log.debug("Прогнозируемый оценка для кандидата {} = {}", candidateId, score);
            recommendedEvents.add(new RecommendedEvent(candidateId, score));
        }
        recommendedEvents = recommendedEvents.stream()
            .sorted(Comparator.comparingDouble(RecommendedEvent::score).reversed())
            .limit(limit)
            .toList();

        log.debug("Рекомендуемые мероприятия для пользователя {}: {}", userId, recommendedEvents);
        return recommendationsMapper.mapToProto(recommendedEvents);
    }

    private Map<Long, List<Neighbor>> buildCandidateNeighborsMap(
        List<Similarity> similarities,
        Set<Long> recommendedCandidateIds,
        Map<Long, Double> weightsMap
    ) {
        Map<Long, List<Neighbor>> candidateNeighborsMap = new HashMap<>();

        for (Similarity s : similarities) {
            long candidateId = recommendedCandidateIds.contains(s.getEventAId()) ? s.getEventAId() : s.getEventBId();
            long neighborId = candidateId == s.getEventAId() ? s.getEventBId() : s.getEventAId();
            double similarity = s.getScore();
            Double weight = weightsMap.get(neighborId);

            if (weight != null) {
                candidateNeighborsMap
                    .computeIfAbsent(candidateId, id -> new ArrayList<>())
                    .add(new Neighbor(neighborId, similarity, weight));
            }
        }
        return candidateNeighborsMap;
    }

    private double calculateScore(List<Neighbor> neighbors) {
        double numerator = 0.0;
        double denominator = 0.0;
        for (Neighbor neighbor : neighbors) {
            numerator += neighbor.weight() * neighbor.similarity();
            denominator += neighbor.similarity();
        }
        return denominator == 0.0 ? 0.0 : numerator / denominator;
    }

    private record Neighbor(
        long eventId,
        double similarity,
        double weight) {
    }
}
