package com.example.expensetracker.dto.account;

import com.example.expensetracker.enums.AccountType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AccountUpdateRequest {
    private String name;          // opcional
    private AccountType type;     // opcional
    private String currencyCode;  // opcional
    private Boolean active;       // opcional (true/false)
}
