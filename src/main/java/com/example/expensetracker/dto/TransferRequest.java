package com.example.expensetracker.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class TransferRequest {

    private Long sourceAccountId;
    private Long destinationAccountId;
    private BigDecimal amount;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate operationDate;
}
