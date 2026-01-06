package com.example.expensetracker.dto.category;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CategoryCreateRequest {

    private String name;
    private String description;
    private String colorHex;
}
