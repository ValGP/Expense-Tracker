package com.example.expensetracker.controller;

import com.example.expensetracker.dto.ExpenseRequest;
import com.example.expensetracker.dto.IncomeRequest;
import com.example.expensetracker.dto.TransferRequest;
import com.example.expensetracker.model.Transaction;
import com.example.expensetracker.service.TransactionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // ------------------------
    // Crear GASTO
    // ------------------------
    @PostMapping("/expense")
    public Transaction createExpense(@RequestBody ExpenseRequest request) {
        return transactionService.createExpense(
                request.getOwnerId(),
                request.getSourceAccountId(),
                request.getCategoryId(),
                request.getAmount(),
                request.getOperationDate(),
                request.getDescription(),
                request.getTagIds()
        );
    }

    // ------------------------
    // Crear INGRESO
    // ------------------------
    @PostMapping("/income")
    public Transaction createIncome(@RequestBody IncomeRequest request) {
        return transactionService.createIncome(
                request.getOwnerId(),
                request.getDestinationAccountId(),
                request.getCategoryId(),
                request.getAmount(),
                request.getOperationDate(),
                request.getDescription(),
                request.getTagIds()
        );
    }

    // ------------------------
    // Crear TRANSFERENCIA
    // ------------------------
    @PostMapping("/transfer")
    public Transaction createTransfer(@RequestBody TransferRequest request) {
        return transactionService.createTransfer(
                request.getOwnerId(),
                request.getSourceAccountId(),
                request.getDestinationAccountId(),
                request.getAmount(),
                request.getOperationDate(),
                request.getDescription()
        );
    }

    // ------------------------
    // Obtener TODAS las transacciones de un usuario
    // GET /api/transactions?ownerId=1
    // ------------------------
    @GetMapping
    public List<Transaction> getTransactionsForUser(@RequestParam Long ownerId) {
        return transactionService.getTransactionsForUser(ownerId);
    }

    // ------------------------
    // Obtener transacciones por período
    // GET /api/transactions/period?ownerId=1&from=2025-12-01&to=2025-12-31
    // ------------------------
    @GetMapping("/period")
    public List<Transaction> getTransactionsForUserInPeriod(
            @RequestParam Long ownerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return transactionService.getTransactionsForUserInPeriod(ownerId, from, to);
    }
}
