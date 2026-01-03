package com.example.expensetracker.api;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class ApiExceptionHandler {

    // 400 - reglas/validaciones manuales
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        ApiError body = ApiError.of(
                400, "Bad Request", ex.getMessage(), req.getRequestURI(), null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // 400 - @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("fieldErrors", ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of(
                        "field", fe.getField(),
                        "message", fe.getDefaultMessage()
                ))
                .toList()
        );

        ApiError body = ApiError.of(
                400,
                "Bad Request",
                "Validation failed",
                req.getRequestURI(),
                details
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // 400 - JSON mal formado / fecha mal / enum inválido
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {

        String message = "Malformed JSON request";
        Throwable cause = ex.getCause();

        if (cause instanceof InvalidFormatException ife) {
            // ejemplo: enum inválido, fecha inválida, tipo inválido
            String field = ife.getPath().isEmpty() ? "unknown" : ife.getPath().get(0).getFieldName();
            Object value = ife.getValue();
            message = "Invalid value for field '" + field + "': " + value;
        }

        ApiError body = ApiError.of(
                400, "Bad Request", message, req.getRequestURI(), null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // 401
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest req) {
        return ResponseEntity.status(401).body(
                ApiError.of(401, "Unauthorized", ex.getMessage(), req.getRequestURI(), null)
        );
    }


    //403
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiError> handleForbidden(ForbiddenException ex, HttpServletRequest req) {
        return ResponseEntity.status(403).body(ApiError.of(403, "Forbidden", ex.getMessage(), req.getRequestURI(), null));
    }

    // 403 - user inactive
    @ExceptionHandler(UserInactiveException.class)
    public ResponseEntity<ApiError> handleUserInactive(UserInactiveException ex, HttpServletRequest req) {
        return ResponseEntity.status(403).body(
                ApiError.of(403, "Forbidden", ex.getMessage(), req.getRequestURI(), null)
        );
    }


    // 404 - si alguna vez usás NoSuchElementException (o lo usás vos)
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiError> handleNotFound(NoSuchElementException ex, HttpServletRequest req) {
        ApiError body = ApiError.of(
                404, "Not Found", ex.getMessage(), req.getRequestURI(), null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    //409
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException ex, HttpServletRequest req) {
        return ResponseEntity.status(409).body(ApiError.of(409, "Conflict", ex.getMessage(), req.getRequestURI(), null));
    }

    // 500 - fallback para que nunca vuelva “500 genérico sin info”
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest req) {
        ApiError body = ApiError.of(
                500,
                "Internal Server Error",
                "Unexpected error",
                req.getRequestURI(),
                Map.of("exception", ex.getClass().getSimpleName())
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
