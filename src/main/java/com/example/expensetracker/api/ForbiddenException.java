package com.example.expensetracker.api;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) { super(message); }
}
