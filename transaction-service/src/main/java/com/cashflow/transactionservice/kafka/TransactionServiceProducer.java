package com.cashflow.transactionservice.kafka;

import com.cashflow.transactionservice.dto.UserModificationKafkaDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceProducer {

    private final KafkaTemplate<String, UserModificationKafkaDto> kafkaTemplate;

    public void sendCustomerIdUpdateEvent(UserModificationKafkaDto userModificationKafkaDto) {
        log.info("Sending customer Id update event: {}", userModificationKafkaDto);
        kafkaTemplate.send("customerId-update-event", userModificationKafkaDto);
    }
}
