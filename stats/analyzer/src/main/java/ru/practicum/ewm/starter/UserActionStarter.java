package ru.practicum.ewm.starter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.service.UserActionServiceImpl;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionStarter {
    private final KafkaConsumer<String, UserActionAvro> consumer;
    private final String userActionsTopic;
    private final UserActionServiceImpl userActionServiceImpl;

    public void start() {
        try {
            consumer.subscribe(List.of(userActionsTopic));
            log.info("Подписались на топик: {}", userActionsTopic);
            while (true) {
                ConsumerRecords<String, UserActionAvro> userActions = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, UserActionAvro> userAction : userActions) {
                    try {
                        logReceivedRecord(userAction);
                        userActionServiceImpl.updateOrCreateWeight(userAction.value());
                    } catch (Exception e) {
                        log.error("Ошибка при обработке действий пользователя: key: {}, value: {}",
                            userAction.key(), userAction.value(), e);
                    }
                }
                consumer.commitSync();
            }
        } catch (WakeupException ignored) {
        } catch (Exception e) {
            log.error("Ошибка при обработке действий пользователя", e);
        } finally {
            closeResources();
        }
    }

    private void logReceivedRecord(ConsumerRecord<String, UserActionAvro> record) {
        log.info("Получено действие пользователя от Kafka: topic: {}, partition: {}, offset: {}, key: {}, timestamp: {}",
            record.topic(),
            record.partition(),
            record.offset(),
            record.key(),
            record.timestamp());
        log.debug("UserAction: {}", record.value());
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
