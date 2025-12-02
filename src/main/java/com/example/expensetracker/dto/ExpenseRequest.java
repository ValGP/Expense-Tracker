package com.example.expensetracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ExpenseRequest {

    private Long ownerId;
    private Long sourceAccountId;
    private Long categoryId;
    private BigDecimal amount;
    private String description;
    private LocalDate operationDate;
    private List<Long> tagIds;

    public Long getOwnerId() {
        return ownerId;
    }

    public Long getSourceAccountId() {
        return sourceAccountId;
    }

    public Long getCategoryId() {
        return categoryId;
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

    public List<Long> getTagIds() {
        return tagIds;
    }
}
