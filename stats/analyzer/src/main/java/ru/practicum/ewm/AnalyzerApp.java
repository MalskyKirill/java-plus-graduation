package ru.practicum.ewm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;
import ru.practicum.ewm.starter.EventSimilarityStarter;
import ru.practicum.ewm.starter.UserActionStarter;

@SpringBootApplication
public class AnalyzerApp {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AnalyzerApp.class, args);

        EventSimilarityStarter eventSimilarityStarter = context.getBean(EventSimilarityStarter.class);
        new Thread(eventSimilarityStarter::start, "event-similarity-starter").start();

        UserActionStarter userActionStarter = context.getBean(UserActionStarter.class);
        new Thread(userActionStarter::start, "user-action-starter").start();
    }
}
