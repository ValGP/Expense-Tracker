package com.example.expensetracker.dto.tag;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TagResponse {

    private Long id;
    private String name;
    private Boolean active;
}
