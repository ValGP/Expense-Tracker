package com.example.expensetracker.dto.account;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AccountBalanceResponse {
    private Long accountId;
    private Long ownerId;
    private String accountName;
    private String currencyCode;
    private BigDecimal currentBalance;
}
