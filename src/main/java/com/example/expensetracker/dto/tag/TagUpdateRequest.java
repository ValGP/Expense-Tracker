package com.example.expensetracker.dto.tag;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TagUpdateRequest {
    private String name;      // opcional
    private Boolean active;   // opcional (para archivar/reactivar)
}
