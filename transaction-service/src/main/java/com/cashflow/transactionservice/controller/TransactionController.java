package com.cashflow.transactionservice.controller;

import com.cashflow.transactionservice.dto.TransactionDto;
import com.cashflow.transactionservice.model.Transaction;
import com.cashflow.transactionservice.requests.CreateTransactionRequest;
import com.cashflow.transactionservice.requests.TransactionUpdateRequest;
import com.cashflow.transactionservice.service.SaltEdgeService;
import com.cashflow.transactionservice.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/v1/transaction-service")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    private final SaltEdgeService saltEdgeService;

    @Value("${internal.key}")
    private String internalKey;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public void createTransaction(@Valid @RequestBody CreateTransactionRequest createTransactionRequest) {
        transactionService.createTransaction(createTransactionRequest);
    }

    @GetMapping("/view/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TransactionDto viewTransaction(@PathVariable Long id) {
        Transaction transaction = transactionService.findTransactionById(id);
        if (!transaction.getUserId().equals(transactionService.getCurrentUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return transactionService.convertTransactionToDto(transaction);
    }

    @GetMapping("/getAllTransactions")
    @ResponseStatus(HttpStatus.OK)
    public List<TransactionDto> getAllTransactionsForCurrentUser() {
        return transactionService.getAllTransactionsForCurrentUser();
    }

    @GetMapping("/getAllTransactions/sorted")
    @ResponseStatus(HttpStatus.OK)
    public List<TransactionDto> getAllTransactionsSorted(@RequestParam String sortBy, @RequestParam String order) {
        return transactionService.getAllTransactionsSorted(sortBy, order);
    }

    @PutMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    public void updateTransaction(@Valid @RequestBody TransactionUpdateRequest transactionUpdateRequest) {
        Transaction transaction = transactionService.findTransactionById(transactionUpdateRequest.getId());
        if (!transaction.getUserId().equals(transactionService.getCurrentUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        transactionService.updateTransaction(transactionUpdateRequest, transaction);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteTransaction(@PathVariable Long id) {
        Transaction transaction = transactionService.findTransactionById(id);
        if (!transaction.getUserId().equals(transactionService.getCurrentUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        transactionService.deleteTransactionById(transaction);
    }

    @DeleteMapping("/deleteAllTransactions")
    @ResponseStatus(HttpStatus.OK)
    public void deleteAllTransactions(@RequestParam Long userId) {
        transactionService.deleteAllTransactionsByUserId(userId);
    }

    @PostMapping("/createConnectionSession")
    @ResponseStatus(HttpStatus.OK)
    public String createConnectionSession() {
        return saltEdgeService.createConnectionSession();
    }

    @PostMapping("/fetchTransactions")
    @ResponseStatus(HttpStatus.OK)
    public void fetchAndSaveTransactions() {
        saltEdgeService.synchronizeTransactions();
    }

    @DeleteMapping("/deleteSaltEdgeUser")
    @ResponseStatus(HttpStatus.OK)
    public void deleteSaltEdgeUser(@RequestHeader("Internal-Key") String key,
                                   @RequestParam String customerId) {
        if (!key.equals(internalKey)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        saltEdgeService.deleteSaltEdgeUser(customerId);
    }
}
