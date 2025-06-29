package ru.practicum.ewm.starter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.service.UserActionService;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {
    private final KafkaConsumer<String, UserActionAvro> consumer;
    private final KafkaProducer<String, SpecificRecordBase> producer;
    private final String userActionsTopic;
    private final UserActionService userActionService;

    public void start() {
        try {
            consumer.subscribe(List.of(userActionsTopic));
            log.info("Подписались на топик: {}", userActionsTopic);
            while (true) {
                ConsumerRecords<String, UserActionAvro> userActions = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, UserActionAvro> userAction : userActions) {
                    try {
                        logReceivedRecord(userAction);
                        userActionService.process(userAction.value());
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
            producer.flush();
            log.info("Данные отправлены в Kafka");
            consumer.commitSync();
            log.info("Все оффсеты закомичены");
        } finally {
            log.info("Закрыли consumer");
            consumer.close();
            log.info("Закрыли producer");
            producer.close();
        }
    }
}
