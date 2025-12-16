package com.example.expensetracker.dto.category;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CategoryCreateRequest {

    private Long ownerId;
    private String name;
    private String description;
    private String colorHex;   // ej "#FF8800"
}
