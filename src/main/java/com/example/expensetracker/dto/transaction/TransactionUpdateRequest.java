package com.example.expensetracker.dto.transaction;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class TransactionUpdateRequest {

    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate operationDate;

    private Long categoryId;

    // null => no tocar tags
    // []   => limpiar tags
    private List<Long> tagIds;
}
