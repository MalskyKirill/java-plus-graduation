package ru.practicum.ewm.config;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.serializer.AvroSerializer;

import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class KafkaProducerConfig {
    private final KafkaPropertiesConfig kafkaPropertiesConfig;
    private KafkaProducer<String, SpecificRecordBase> producer;

    @Bean
    KafkaProducer<String, SpecificRecordBase> kafkaProducer() {
        if (producer == null) {
            Properties config = new Properties();
            config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaPropertiesConfig.getBootstrapServer());
            config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, AvroSerializer.class.getName());
            config.put(ProducerConfig.RETRIES_CONFIG, kafkaPropertiesConfig.getRetriesCount());
            config.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, kafkaPropertiesConfig.getRetryBackOffMs());
            config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, kafkaPropertiesConfig.getIsIdempotence());
            config.put(ProducerConfig.BATCH_SIZE_CONFIG, kafkaPropertiesConfig.getBatchSize());
            config.put(ProducerConfig.LINGER_MS_CONFIG, kafkaPropertiesConfig.getLingerMs());
            producer = new KafkaProducer<>(config);
        }
        return producer;
    }

    @Bean
    public String userActionsTopic() {
        return kafkaPropertiesConfig.getTopic();
    }

    @PreDestroy
    public void closeProducer() {
        if (producer != null) {
            producer.close();
        }
    }
}
