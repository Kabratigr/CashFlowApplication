package com.cashflow.userservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "transaction-service", path = "/v1/transaction-service")
public interface TransactionServiceClient {

    @DeleteMapping("/deleteSaltEdgeUser")
    void deleteSaltEdgeUser(@RequestParam String customerId);

    @DeleteMapping("/deleteAllTransactions")
    void deleteAllTransactions(@RequestParam Long userId);
}
