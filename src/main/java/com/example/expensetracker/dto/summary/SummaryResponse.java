package com.example.expensetracker.dto.summary;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class SummaryResponse {

    private Long ownerId;
    private LocalDate from;
    private LocalDate to;

    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal net;

    private List<CategoryTotalResponse> topCategories;
}
