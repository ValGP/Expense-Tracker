package com.example.expensetracker.dto.account;

import com.example.expensetracker.enums.AccountType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class AccountCreateRequest {

    private String name;
    private AccountType type;
    private String currencyCode;
    private BigDecimal initialBalance;
}
