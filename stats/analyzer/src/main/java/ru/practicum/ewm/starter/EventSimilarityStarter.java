package ru.practicum.ewm.starter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.service.EventSimilarityService;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventSimilarityStarter {
    private final KafkaConsumer<String, EventSimilarityAvro> consumer;
    private final String eventSimilarityTopic;
    private final EventSimilarityService eventSimilarityService;

    public void start() {
        try {
            consumer.subscribe(List.of(eventSimilarityTopic));
            log.info("Подписались на топик: {}", eventSimilarityTopic);
            while (true) {
                ConsumerRecords<String, EventSimilarityAvro> similarities = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, EventSimilarityAvro> record : similarities) {
                    try {
                        logReceivedRecord(record);
                        eventSimilarityService.updateOrCreateSimilarity(record.value());
                    } catch (Exception e) {
                        log.error("Ошибка при обработке сходства событий: key: {}, value: {}",
                            record.key(), record.value(), e);
                    }
                }
                consumer.commitSync();
            }
        } catch (WakeupException ignored) {
        } catch (Exception e) {
            log.error("Ошибка при обработке сходства событий", e);
        } finally {
            closeResources();
        }
    }

    private void logReceivedRecord(ConsumerRecord<String, EventSimilarityAvro> record) {
        log.info("Получена запись о сходстве от Kafka: topic: {}, partition: {}, offset: {}, key: {}, timestamp: {}",
            record.topic(),
            record.partition(),
            record.offset(),
            record.key(),
            record.timestamp());
        log.debug("EventSimilarity: {}", record.value());
    }

    private void closeResources() {
        try {
            consumer.commitSync();
            log.info("Все оффсеты закомичены");
        } finally {
            log.info("Закрыли consumer");
            consumer.close();
        }
    }
}
