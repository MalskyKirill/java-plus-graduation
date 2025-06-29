package ru.practicum.ewm.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregatorSimilarityHandler {
    private final KafkaProducer<String, SpecificRecordBase> producer;
    private final String eventSimilarityTopic;

    public void sendSimilarity(long eventAId, long eventBId, float similarity) {
        EventSimilarityAvro eventSimilarityAvro = buildAvro(eventAId, eventBId, similarity);
        ProducerRecord<String, SpecificRecordBase> producerRecord = buildProducerRecord(eventSimilarityAvro);
        logProducerRecord(producerRecord);
        producer.send(producerRecord, this::handleCallback);
    }

    private EventSimilarityAvro buildAvro(long eventAId, long eventBId, float similarity) {
        return EventSimilarityAvro.newBuilder()
            .setEventA(Long.min(eventAId, eventBId))
            .setEventB(Long.max(eventAId, eventBId))
            .setScore(similarity)
            .setTimestamp(Instant.now())
            .build();
    }

    private ProducerRecord<String, SpecificRecordBase> buildProducerRecord(EventSimilarityAvro eventSimilarityAvro) {
        return new ProducerRecord<>(
            eventSimilarityTopic,
            null,
            eventSimilarityAvro.getTimestamp().toEpochMilli(),
            null,
            eventSimilarityAvro
        );
    }

    private void logProducerRecord(ProducerRecord<String, SpecificRecordBase> producerRecord) {
        log.info("Отправить ProducerRecord: topic={}, key={}, partition={}, timestamp={}",
            producerRecord.topic(),
            producerRecord.key(),
            producerRecord.partition() != null ? producerRecord.partition() : "Auto partition assignment",
            producerRecord.timestamp() != null ? producerRecord.timestamp() : "Not set");
        log.debug("ProducerRecord: {}", producerRecord);
    }

    private void handleCallback(RecordMetadata metadata, Exception exception) {
        if (exception != null) {
            log.error("Ошибка при отправке сообщения в Kafka: {}", exception.getMessage(), exception);
        } else {
            log.info("Сообщение отправлено в Kafka: topic={}, offset={}", metadata.topic(), metadata.offset());
        }
    }
}
