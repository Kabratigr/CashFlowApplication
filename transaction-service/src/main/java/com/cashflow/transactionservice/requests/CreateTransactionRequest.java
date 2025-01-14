package com.cashflow.transactionservice.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateTransactionRequest {
    @NotBlank(message = "Title is required")
    private String title;
    @NotNull(message = "Amount is required")
    private BigDecimal amount;
    @NotBlank(message = "Category is required")
    private String category;
}
