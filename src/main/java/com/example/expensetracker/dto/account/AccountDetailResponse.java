package com.example.expensetracker.dto.account;

import com.example.expensetracker.dto.transaction.TransactionResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AccountDetailResponse {

    private AccountSummaryResponse account;
    private List<TransactionResponse> transactions;
}
