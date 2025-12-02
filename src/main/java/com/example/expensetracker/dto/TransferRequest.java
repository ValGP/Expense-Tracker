package com.example.expensetracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransferRequest {

    private Long ownerId;
    private Long sourceAccountId;
    private Long destinationAccountId;
    private BigDecimal amount;
    private String description;
    private LocalDate operationDate;

    public Long getOwnerId() {
        return ownerId;
    }

    public Long getSourceAccountId() {
        return sourceAccountId;
    }

    public Long getDestinationAccountId() {
        return destinationAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getOperationDate() {
        return operationDate;
    }
}
