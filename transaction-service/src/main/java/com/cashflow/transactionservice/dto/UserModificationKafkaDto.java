package com.cashflow.transactionservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserModificationKafkaDto {
    private Long userId;
    private String customerId;
}
