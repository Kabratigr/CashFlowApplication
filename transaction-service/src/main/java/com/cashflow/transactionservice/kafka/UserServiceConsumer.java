package com.cashflow.transactionservice.kafka;

import com.cashflow.transactionservice.dto.UserModificationKafkaDto;
import com.cashflow.transactionservice.service.SaltEdgeService;
import com.cashflow.transactionservice.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceConsumer {

    private final TransactionService transactionService;

    private final SaltEdgeService saltEdgeService;

    @KafkaListener(topics = "user-deletion-topic", groupId = "transaction-group")
    public void handleUserDeletion(UserModificationKafkaDto userModificationKafkaDto) {
        log.info("Received user deletion event: {}", userModificationKafkaDto);
        transactionService.deleteAllTransactionsByUserId(userModificationKafkaDto.getUserId());
        log.info("Deleted transactions for user: {}", userModificationKafkaDto.getUserId());
        if (userModificationKafkaDto.getCustomerId() != null) {
            log.info("Deleting SaltEdge user with customer Id: {}", userModificationKafkaDto.getCustomerId());
            saltEdgeService.deleteSaltEdgeUser(userModificationKafkaDto.getCustomerId());
        }
    }
}
