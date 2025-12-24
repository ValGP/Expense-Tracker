package com.example.expensetracker.dto.summary;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CategoryTotalResponse {
    private Long categoryId;
    private String categoryName;
    private BigDecimal total;
}
