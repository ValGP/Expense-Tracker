package com.example.expensetracker.dto.account;

import com.example.expensetracker.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AccountResponse {

    private Long id;
    private String name;
    private AccountType type;
    private String currencyCode;
    private BigDecimal initialBalance;
    private Boolean active;
}
