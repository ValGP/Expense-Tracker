package com.example.expensetracker.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class ExpenseRequest {

    private Long ownerId;
    private Long sourceAccountId;
    private Long categoryId;
    private BigDecimal amount;
    private String description;

    // 👇 para que Jackson sepa cómo parsear la fecha
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate operationDate;

    private List<Long> tagIds;
}
