package com.cashflow.kafkamanagementservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaTopicInitializer implements ApplicationRunner {

    private final AdminClient adminClient;

    @Override
    public void run(ApplicationArguments args) {
        createTopicIfNotExist("user-deletion-topic", 3, (short) 1);
        createTopicIfNotExist("customerId-update-event", 3, (short) 1);
    }

    private void createTopicIfNotExist(String topicName, int partitions, short replicationFactor) {
        try {
            Set<String> existingTopics = adminClient.listTopics().names().get();
            if (!existingTopics.contains(topicName)) {
                NewTopic newTopic = new NewTopic(topicName, partitions, replicationFactor);
                adminClient.createTopics(Collections.singleton(newTopic)).all().get();
                log.info("Created topic {}", topicName);
            } else {
                log.info("Topic {} already exists", topicName);
            }
        } catch (Exception exception) {
            log.error("Failed to create topic {}", topicName, exception);
        }
    }
}
