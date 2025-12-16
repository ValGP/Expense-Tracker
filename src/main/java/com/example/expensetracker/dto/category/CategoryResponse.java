package com.example.expensetracker.dto.category;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategoryResponse {

    private Long id;
    private String name;
    private String description;
    private String colorHex;
    private Boolean active;
}
