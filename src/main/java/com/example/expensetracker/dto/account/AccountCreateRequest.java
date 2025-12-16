package com.example.expensetracker.dto.account;

import com.example.expensetracker.enums.AccountType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class AccountCreateRequest {

    private Long ownerId;
    private String name;
    private AccountType type;

    // por ahora asumimos misma moneda del user (o ARS del seed)
    private String currencyCode;

    private BigDecimal initialBalance;
}
