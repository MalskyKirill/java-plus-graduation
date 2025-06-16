package ru.practicum.ewm.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Properties;

import ru.practicum.ewm.deserializer.UserActionAvroDeserializer;
import ru.practicum.ewm.deserializer.EventSimilarityAvroDeserializer;

@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {
    private final KafkaPropertiesConfig config;

    @Bean
    public KafkaConsumer<String, UserActionAvro> userActionConsumer() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServer());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, config.getUserActionsGroupId());
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, UserActionAvroDeserializer.class.getName());
        return new KafkaConsumer<>(properties);
    }

    @Bean
    public KafkaConsumer<String, EventSimilarityAvro> similarityConsumer() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServer());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, config.getEventSimilarityGroupId());
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, EventSimilarityAvroDeserializer.class.getName());
        return new KafkaConsumer<>(properties);
    }
}
