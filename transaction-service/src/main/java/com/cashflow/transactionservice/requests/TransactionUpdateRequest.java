package com.cashflow.transactionservice.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionUpdateRequest {
    @NotNull(message = "Id is required")
    private Long id;
    private String title;
    private BigDecimal amount;
    private String category;
}
