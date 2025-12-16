package com.example.expensetracker.dto.tag;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TagCreateRequest {

    private Long ownerId;
    private String name;
}
