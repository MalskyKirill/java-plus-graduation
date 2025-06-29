package ru.practicum.ewm.config;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "kafka")
@Getter
@Setter
@Validated
public class KafkaPropertiesConfig {
    @NotNull
    private String bootstrapServer;

    @NotNull
    private String topic;

    @Positive
    private Integer retriesCount;

    @Positive
    private Integer retryBackOffMs;

    private Boolean isIdempotence;

    @Positive
    private Integer batchSize;

    @Positive
    private Integer lingerMs;
}
