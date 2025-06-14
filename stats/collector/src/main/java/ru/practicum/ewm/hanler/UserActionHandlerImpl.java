package ru.practicum.ewm.hanler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.grpc.stats.event.ActionTypeProto;
import ru.practicum.ewm.grpc.stats.event.UserActionProto;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActionHandlerImpl implements UserActionHandler{
    private final KafkaProducer<String, SpecificRecordBase> producer;
    private final String userActionsTopic;

    @Override
    public void handle(UserActionProto userActionProto) {
        UserActionAvro userActionAvro = toAvro(userActionProto);
        ProducerRecord<String, SpecificRecordBase> record = buildProducerRecord(userActionAvro);
        logProducerRecord(record);
        producer.send(record, this::handleCallback);
    }

    private UserActionAvro toAvro(UserActionProto userActionProto) {
        UserActionAvro avro = UserActionAvro.newBuilder()
            .setUserId(userActionProto.getUserId())
            .setEventId(userActionProto.getEventId())
            .setActionType(getActionType(userActionProto.getActionType()))
            .setTimestamp(mapToInstant(userActionProto))
            .build();
        log.debug("Преобразование UserActionProto в UserActionAvro: {}", avro);
        return avro;
    }

    private ActionTypeAvro getActionType(ActionTypeProto actionTypeProto) {
        return switch (actionTypeProto) {
            case ACTION_VIEW -> ActionTypeAvro.VIEW;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            case UNRECOGNIZED -> null;
        };
    }

    private ProducerRecord<String, SpecificRecordBase> buildProducerRecord(UserActionAvro userActionAvro) {
        return new ProducerRecord<>(
            userActionsTopic,
            null,
            userActionAvro.getTimestamp().toEpochMilli(),
            null,
            userActionAvro
        );
    }

    private void logProducerRecord(ProducerRecord<String, SpecificRecordBase> producerRecord) {
        log.info("Отправка ProducerRecord: topic={}, key={}, partition={}, timestamp={}",
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

    private Instant mapToInstant(UserActionProto userActionProto) {
        return Instant.ofEpochSecond(userActionProto.getTimestamp().getSeconds(),
            userActionProto.getTimestamp().getNanos());
    }
}
