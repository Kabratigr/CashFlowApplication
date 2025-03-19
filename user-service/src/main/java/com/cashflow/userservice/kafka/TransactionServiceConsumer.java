package com.cashflow.userservice.kafka;

import com.cashflow.userservice.dto.UserModificationKafkaDto;
import com.cashflow.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceConsumer {

    private final UserService userService;

    @KafkaListener(topics = "customerId-update-event", groupId = "user-group")
    public void handleCustomerIdUpdate(UserModificationKafkaDto userModificationKafkaDto) {
        log.info("Received customer Id update event: {}", userModificationKafkaDto);
        userService.updateCustomerId(userModificationKafkaDto);
    }
}
