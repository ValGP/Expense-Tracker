package com.example.expensetracker.api;

public class UserInactiveException extends RuntimeException {
    public UserInactiveException() {
        super("User is inactive");
    }
}