package com.example.expensetracker.api;

public class EmailAlreadyExistsException extends ConflictException {
    public EmailAlreadyExistsException(String email) {
        super("Email already registered: " + email);
    }
}
