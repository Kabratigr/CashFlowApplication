package com.cashflow.transactionservice.repository;

import com.cashflow.transactionservice.dto.TransactionDto;
import com.cashflow.transactionservice.model.Transaction;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findTransactionById(Long id);
    @Query("SELECT new com.cashflow.transactionservice.dto.TransactionDto(t.title, t.amount, t.category, t.createdAt) " +
            "FROM Transaction t WHERE t.userId = :userId")
    List<TransactionDto> findAllByUserIdAsDto(@Param("userId") Long userId);
    @Query("SELECT new com.cashflow.transactionservice.dto.TransactionDto(t.title, t.amount, t.category, t.createdAt) " +
            "FROM Transaction t WHERE t.userId = :userId")
    List<TransactionDto> findAllByUserIdAsDtoSorted(@Param("userId") Long userId, Sort sort);
    void deleteAllByUserId(Long userId);
}
