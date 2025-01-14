package com.cashflow.transactionservice.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionDto {
    private String title;
    private BigDecimal amount;
    private String category;
    private LocalDateTime createdAt;
}
