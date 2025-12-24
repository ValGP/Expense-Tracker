package com.example.expensetracker.controller;

import com.example.expensetracker.dto.ExpenseRequest;
import com.example.expensetracker.dto.IncomeRequest;
import com.example.expensetracker.dto.TransferRequest;
import com.example.expensetracker.service.TransactionService;
import com.example.expensetracker.dto.transaction.TransactionResponse;
import com.example.expensetracker.dto.transaction.TransactionUpdateRequest;
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
    public TransactionResponse createExpense(@RequestBody ExpenseRequest request) {
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
    public TransactionResponse createIncome(@RequestBody IncomeRequest request) {
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
    public TransactionResponse createTransfer(@RequestBody TransferRequest request) {
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
    // Cancel or Confirm Transaction
    // ------------------------
    @PatchMapping("/{id}/cancel")
    public TransactionResponse cancel(@PathVariable Long id) {
        return transactionService.cancel(id);
    }

    @PatchMapping("/{id}/confirm")
    public TransactionResponse confirm(@PathVariable Long id) {
        return transactionService.confirm(id);
    }


    // ------------------------
    // Obtener TODAS las transacciones de un usuario
    // GET /api/transactions?ownerId=1
    // ------------------------
    @GetMapping
    public List<TransactionResponse> getTransactionsForUser(@RequestParam Long ownerId) {
        return transactionService.getTransactionsForUser(ownerId);
    }

    // ------------------------
    // Obtener transacciones por período
    // GET /api/transactions/period?ownerId=1&from=2025-12-01&to=2025-12-31
    // ------------------------
    @GetMapping("/period")
    public List<TransactionResponse> getTransactionsForUserInPeriod(
            @RequestParam Long ownerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return transactionService.getTransactionsForUserInPeriod(ownerId, from, to);
    }

    // ------------------------
    // MODIFICAR TRANSACCION
    // ------------------------

    @PatchMapping("/{id}")
    public TransactionResponse updatePartial(@PathVariable Long id,
                                             @RequestBody TransactionUpdateRequest req) {
        return transactionService.update(id, req);
    }
}
