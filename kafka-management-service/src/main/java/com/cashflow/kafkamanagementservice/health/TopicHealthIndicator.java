package com.cashflow.kafkamanagementservice.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class TopicHealthIndicator implements HealthIndicator {

    private final KafkaAdmin kafkaAdmin;

    private final List<String> requiredTopics = List.of(
            "user-deletion-topic",
            "customerId-update-event"
    );

    @Override
    public Health health() {
        try {
            Set<String> existingTopics = kafkaAdmin.describeTopics().keySet();
            List<String> missingTopics = requiredTopics.stream()
                    .filter(topic -> !existingTopics.contains(topic))
                    .toList();
            if (!missingTopics.isEmpty()) {
                log.error("Missing topics: {}", missingTopics);
                return Health.down()
                        .withDetail("missingTopics", missingTopics)
                        .build();
            }
            log.info("Existing topics: {}", existingTopics);
            return Health.up()
                    .withDetail("message", "All required topics exist")
                    .build();
        } catch (Exception exception) {
            log.error("Kafka health check failed", exception);
            return Health.down()
                    .withDetail("Error", exception)
                    .build();
        }
    }
}
