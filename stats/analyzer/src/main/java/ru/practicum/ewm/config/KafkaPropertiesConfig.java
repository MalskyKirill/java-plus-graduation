package ru.practicum.ewm.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
public class KafkaPropertiesConfig {
    @Value("${kafka.bootstrap-server}")
    private String bootstrapServer;

    @Value("${kafka.topic.actions}")
    private String userActionsTopic;

    @Value("${kafka.topic.similarity}")
    private String eventSimilarityTopic;

    @Value("${kafka.group-id.user-actions}")
    private String userActionsGroupId;

    @Value("${kafka.group-id.event-similarity}")
    private String eventSimilarityGroupId;

    @Bean
    public String userActionsTopic() {
        return userActionsTopic;
    }

    @Bean
    public String eventSimilarityTopic() {
        return eventSimilarityTopic;
    }
}
