package com.cashflow.userservice.kafka;

import com.cashflow.userservice.dto.UserModificationKafkaDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceProducer {

    private final KafkaTemplate<String, UserModificationKafkaDto> kafkaTemplate;

    public void sendUserDeletionEvent(UserModificationKafkaDto userModificationKafkaDto) {
        log.info("Sending user deletion event: {}", userModificationKafkaDto);
        kafkaTemplate.send("user-deletion-topic", userModificationKafkaDto);
    }
}
