package com.example.expensetracker.dto.category;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CategoryUpdateRequest {
    private String name;        // opcional
    private String description; // opcional
    private String colorHex;    // opcional (ej: #FF8800)
    private Boolean active;     // opcional
}
