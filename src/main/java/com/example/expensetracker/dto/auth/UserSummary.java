package com.example.expensetracker.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class UserSummary {
    private Long id;
    private String email;
    private String name;
    private Set<String> roles;
}
