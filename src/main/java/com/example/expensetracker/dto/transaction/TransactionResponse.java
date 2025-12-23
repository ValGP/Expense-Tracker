package com.example.expensetracker.dto.transaction;

import com.example.expensetracker.enums.TransactionState;
import com.example.expensetracker.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class TransactionResponse {

    private Long id;
    private Long ownerId;

    private TransactionType type;
    private TransactionState state;

    private BigDecimal amount;
    private LocalDate operationDate;
    private LocalDateTime recordedAt;

    private String description;

    private Long sourceAccountId;
    private Long destinationAccountId;

    private Long categoryId;
    private List<Long> tagIds;
}
