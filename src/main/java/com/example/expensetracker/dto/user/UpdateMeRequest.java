package com.example.expensetracker.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateMeRequest(
        @Size(min = 1, max = 120, message = "name must be between 1 and 120 chars")
        String name,

        @Email(message = "email must be valid")
        @Size(max = 200, message = "email must be <= 200 chars")
        String email
) {}
