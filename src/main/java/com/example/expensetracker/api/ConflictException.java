package com.example.expensetracker.api;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) { super(message); }
}

