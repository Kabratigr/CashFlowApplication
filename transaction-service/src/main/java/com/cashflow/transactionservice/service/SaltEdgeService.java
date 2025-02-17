package com.cashflow.transactionservice.service;

import com.cashflow.transactionservice.client.UserServiceClient;
import com.cashflow.transactionservice.model.Transaction;
import com.cashflow.transactionservice.repository.TransactionRepository;
import com.cashflow.transactionservice.requests.UserUpdateCustomerIdRequest;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SaltEdgeService {

    private final WebClient webClient;

    private final UserServiceClient userServiceClient;

    private final TransactionRepository transactionRepository;

    private String createCustomerId(Long currentUserId) {
        String customerId = webClient.post()
                .uri("/customers")
                .bodyValue(Map.of("data", Map.of("identifier", "CashFlowAppUserId-" + currentUserId)))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(response -> response.path("data").path("id").asText())
                .blockOptional()
                .orElseThrow(() -> new RuntimeException("SaltEdge Service failed to create customer id"));
        userServiceClient.updateCustomerId(currentUserId, new UserUpdateCustomerIdRequest(customerId));
        return customerId;
    }

    public String createConnectionSession() {
        String customerId = userServiceClient.getUserCustomerIdById(Long.valueOf(getCurrentUserId())).getCustomerId();
        if (customerId == null) {
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
        return webClient.post()
                .uri("/connect_sessions/create")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(response -> response.path("data").path("connect_url").asText())
                .blockOptional()
                .orElseThrow(() -> new RuntimeException("SaltEdge Service failed to create connection session"));
    }

    private String getConnectionId(String customerId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/connections")
                        .queryParam("customer_id", customerId)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(response -> {
                    JsonNode connections = response.path("data");
                    if (connections.isArray() && !connections.isEmpty()) {
                        return connections.get(0).path("id").asText();
                    } else {
                        throw new RuntimeException("SaltEdge Service failed to find connections");
                    }
                })
                .block();
    }

    private String getAccountId(String connectionId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/accounts")
                        .queryParam("connection_id", connectionId)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(response -> response.path("data").path("id").asText())
                .blockOptional()
                .orElseThrow(() -> new RuntimeException("SaltEdge Service failed to find account id"));
    }

    private void fetchAndSaveTransactions(String connectionId, String accountId) {
        JsonNode transactionResponse = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/transactions")
                        .queryParam("connection_id", connectionId)
                        .queryParam("account_id", accountId)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
        if (transactionResponse != null && transactionResponse.has("data")) {
            JsonNode transactionData = transactionResponse.get("data");
            List<Transaction> transactions = new ArrayList<>();
            transactionData.forEach(transaction -> {
                Transaction transactionToSave = mapToTransaction(transaction);
                transactions.add(transactionToSave);
            });
            transactionRepository.saveAll(transactions);
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

    public void synchronizeTransactions() {
        Long currentUserId = Long.valueOf(getCurrentUserId());
        String customerId = userServiceClient.getUserCustomerIdById(currentUserId).getCustomerId();
        if (customerId == null) customerId = createCustomerId(currentUserId);
        String connectionId = getConnectionId(customerId);
        String accountId = getAccountId(connectionId);
        fetchAndSaveTransactions(connectionId, accountId);
    }

    public void deleteSaltEdgeUser(String customerId) {
        webClient.delete()
                .uri("/customers/" + customerId)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
