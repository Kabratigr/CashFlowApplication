package com.cashflow.transactionservice.service;

import com.cashflow.transactionservice.client.UserServiceClient;
import com.cashflow.transactionservice.kafka.TransactionServiceProducer;
import com.cashflow.transactionservice.model.Transaction;
import com.cashflow.transactionservice.repository.TransactionRepository;
import com.cashflow.transactionservice.dto.UserModificationKafkaDto;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaltEdgeService {

    private final RestTemplate restTemplate;

    private final UserServiceClient userServiceClient;

    private final TransactionRepository transactionRepository;

    private final TransactionServiceProducer transactionServiceProducer;

    private String createCustomerId(Long currentUserId) {
        log.info("Creating customer ID for user: {}", currentUserId);
        Map<String, Object> requestBody = Map.of("data", Map.of("identifier", "CashFlowAppUserId-" + currentUserId));
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                "https://www.saltedge.com/api/v5/customers", requestBody, JsonNode.class);
        String customerId = Optional.ofNullable(response.getBody())
                .map(body -> body.path("data").path("id").asText())
                .orElseThrow(() -> new RuntimeException("SaltEdge Service failed to create customer id"));
        log.info("Successfully created customer ID: {}", customerId);
        transactionServiceProducer.sendCustomerIdUpdateEvent(new UserModificationKafkaDto(currentUserId, customerId));
        return customerId;
    }

    public String createConnectionSession() {
        log.info("Creating connection session for user: {}", getCurrentUserId());
        String customerId = userServiceClient.getUserCustomerIdById(Long.valueOf(getCurrentUserId())).getCustomerId();
        if (customerId == null) {
            log.warn("Customer ID not found, creating a new one.");
            customerId = createCustomerId(Long.valueOf(getCurrentUserId()));
        }
        Map<String, Object> requestBody = Map.of(
                "data", Map.of(
                        "customer_id", customerId,
                        "consent", Map.of(
                                "from_date", LocalDate.now().toString(),
                                "period_days", 90,
                                "scopes", List.of("account_details", "transactions_details")
                        ),
                        "attempt", Map.of(
                                "from_date", LocalDate.now().toString(),
                                "fetch_scopes", List.of("accounts", "transactions")
                        )
                ));
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                "https://www.saltedge.com/api/v5/connect_sessions/create", requestBody, JsonNode.class);
        String connectUrl = Optional.ofNullable(response.getBody())
                .map(body -> body.path("data").path("connect_url").asText())
                .orElseThrow(() -> new RuntimeException("SaltEdge Service failed to create connection session"));
        log.info("Successfully created connection session: {}", connectUrl);
        return connectUrl;
    }

    private String getConnectionId(String customerId) {
        log.info("Fetching connection ID for customer: {}", customerId);
        String url = "https://www.saltedge.com/api/v5/connections?customer_id=" + customerId;
        ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
        return Optional.ofNullable(response.getBody())
                .map(body -> body.path("data"))
                .filter(connections -> connections.isArray() && !connections.isEmpty())
                .map(connections -> {
                    String connectionId = connections.get(0).path("id").asText();
                    log.info("Successfully fetched connection ID: {}", connectionId);
                    return connectionId;
                })
                .orElseThrow(() -> new RuntimeException("SaltEdge Service failed to find connections"));
    }

    private List<String> getAccountId(String connectionId) {
        log.info("Fetching account IDs for connection: {}", connectionId);
        String url = "https://www.saltedge.com/api/v5/accounts?connection_id=" + connectionId;
        ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
        return Optional.ofNullable(response.getBody())
                .map(body -> {
                    List<String> accountIds = new ArrayList<>();
                    JsonNode dataArray = body.path("data");
                    if (dataArray.isArray()) {
                        dataArray.forEach(account -> accountIds.add(account.path("id").asText()));
                    }
                    log.info("Found account IDs: {}", accountIds);
                    return accountIds;
                })
                .orElseThrow(() -> new RuntimeException("SaltEdge Service failed to find account id"));
    }

    private void fetchAndSaveTransactions(String connectionId, List<String> accountIds) {
        log.info("Fetching transactions for connection: {} and accounts: {}", connectionId, accountIds);
        List<JsonNode> transactions = new ArrayList<>();
        for (String accountId : accountIds) {
            String url = "https://www.saltedge.com/api/v5/transactions?connection_id=" + connectionId + "&account_id=" + accountId;
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
            Optional.ofNullable(response.getBody())
                    .map(body -> body.path("data"))
                    .filter(JsonNode::isArray)
                    .ifPresent(array -> array.forEach(transactions::add));
        }
        if (!transactions.isEmpty()) {
            List<Transaction> transactionsToSave = transactions.stream()
                    .map(this::mapToTransaction)
                    .toList();
            transactionRepository.saveAll(transactionsToSave);
            log.info("Successfully saved {} transactions", transactionsToSave.size());
        }
    }

    public void synchronizeTransactions() {
        Long currentUserId = Long.valueOf(getCurrentUserId());
        log.info("Synchronizing transactions for user: {}", currentUserId);
        String customerId = userServiceClient.getUserCustomerIdById(currentUserId).getCustomerId();
        if (customerId == null) customerId = createCustomerId(currentUserId);
        String connectionId = getConnectionId(customerId);
        List<String> accountIds = getAccountId(connectionId);
        if (accountIds != null && !accountIds.isEmpty()) fetchAndSaveTransactions(connectionId, accountIds);
    }

    public void deleteSaltEdgeUser(String customerId) {
        String url = "https://www.saltedge.com/api/v5/customers/" + customerId;
        try {
            restTemplate.delete(url);
            transactionServiceProducer.sendCustomerIdUpdateEvent(
                    new UserModificationKafkaDto(Long.valueOf(getCurrentUserId()), null));
        } catch (HttpClientErrorException.NotFound exception) {
            log.warn("SaltEdge customer {} not found, skipping deletion.", customerId);
        }
    }

    private Transaction mapToTransaction(JsonNode transactionResponse) {
        return Transaction.builder()
                .userId(Long.valueOf(getCurrentUserId()))
                .title(transactionResponse.path("description").asText())
                .amount(transactionResponse.path("amount").decimalValue())
                .category(transactionResponse.path("category").asText())
                .build();
    }

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
