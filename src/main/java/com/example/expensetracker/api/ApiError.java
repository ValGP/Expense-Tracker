package com.example.expensetracker.api;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
public class ApiError {
    private String timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private Map<String, Object> details;

    public static ApiError of(int status, String error, String message, String path, Map<String, Object> details) {
        return new ApiError(OffsetDateTime.now().toString(), status, error, message, path, details);
    }
}
