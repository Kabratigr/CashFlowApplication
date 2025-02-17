package com.cashflow.transactionservice.service;

import com.cashflow.transactionservice.dto.TransactionDto;
import com.cashflow.transactionservice.exceptions.InvalidSortOrderException;
import com.cashflow.transactionservice.exceptions.TransactionNotFoundException;
import com.cashflow.transactionservice.model.Transaction;
import com.cashflow.transactionservice.repository.TransactionRepository;
import com.cashflow.transactionservice.requests.CreateTransactionRequest;
import com.cashflow.transactionservice.requests.TransactionUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public void createTransaction(CreateTransactionRequest createTransactionRequest) {
        Transaction transactionToCreate = Transaction.builder()
                .userId(getCurrentUserId())
                .title(createTransactionRequest.getTitle())
                .amount(createTransactionRequest.getAmount())
                .category(createTransactionRequest.getCategory())
                .build();
        transactionRepository.save(transactionToCreate);
    }

    public TransactionDto convertTransactionToDto(Transaction transaction) {
        return TransactionDto.builder()
                .title(transaction.getTitle())
                .amount(transaction.getAmount())
                .category(transaction.getCategory())
                .build();
    }

    public List<TransactionDto> getAllTransactionsForCurrentUser() {
        return transactionRepository.findAllByUserIdAsDto(getCurrentUserId());
    }

    public List<TransactionDto> getAllTransactionsSorted(String sortBy, String order) {
        Sort sort = switch (sortBy) {
            case "amount" -> Sort.by(Sort.Direction.fromString(order), "amount");
            case "createdAt" -> Sort.by(Sort.Direction.fromString(order), "createdAt");
            default -> throw new InvalidSortOrderException("Invalid sorting parameter");
        };
        return transactionRepository.findAllByUserIdAsDtoSorted(getCurrentUserId(), sort);
    }

    public void updateTransaction(TransactionUpdateRequest transactionUpdateRequest, Transaction transactionToUpdate) {
        transactionToUpdate.setTitle(Optional.ofNullable(transactionUpdateRequest.getTitle()).orElse(transactionToUpdate.getTitle()));
        transactionToUpdate.setAmount(Optional.ofNullable(transactionUpdateRequest.getAmount()).orElse(transactionToUpdate.getAmount()));
        transactionToUpdate.setCategory(Optional.ofNullable(transactionUpdateRequest.getCategory()).orElse(transactionToUpdate.getCategory()));
        transactionRepository.save(transactionToUpdate);
    }

    public void deleteTransactionById(Transaction transactionToDelete) {
        transactionRepository.delete(transactionToDelete);
    }

    @Transactional
    public void deleteAllTransactionsByUserId(Long userId) {
        transactionRepository.deleteAllByUserId(userId);
    }

    public Transaction findTransactionById(Long id) {
        return transactionRepository.findTransactionById(id)
                .orElseThrow(() -> new TransactionNotFoundException(
                        String.format("Transaction with id \"%s\" not found", id)));
    }

    public Long getCurrentUserId() {
        return Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
