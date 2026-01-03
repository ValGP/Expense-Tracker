package com.example.expensetracker.controller;

import com.example.expensetracker.dto.ExpenseRequest;
import com.example.expensetracker.dto.IncomeRequest;
import com.example.expensetracker.dto.TransferRequest;
import com.example.expensetracker.dto.transaction.TransactionResponse;
import com.example.expensetracker.dto.transaction.TransactionUpdateRequest;
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
    // Crear GASTO (del usuario autenticado)
    // ------------------------
    @PostMapping("/expense")
    public TransactionResponse createExpense(@RequestBody ExpenseRequest request) {
        return transactionService.createExpense(
                request.getSourceAccountId(),
                request.getCategoryId(),
                request.getAmount(),
                request.getOperationDate(),
                request.getDescription(),
                request.getTagIds()
        );
    }

    // ------------------------
    // Crear INGRESO (del usuario autenticado)
    // ------------------------
    @PostMapping("/income")
    public TransactionResponse createIncome(@RequestBody IncomeRequest request) {
        return transactionService.createIncome(
                request.getDestinationAccountId(),
                request.getCategoryId(),
                request.getAmount(),
                request.getOperationDate(),
                request.getDescription(),
                request.getTagIds()
        );
    }

    // ------------------------
    // Crear TRANSFERENCIA (del usuario autenticado)
    // ------------------------
    @PostMapping("/transfer")
    public TransactionResponse createTransfer(@RequestBody TransferRequest request) {
        return transactionService.createTransfer(
                request.getSourceAccountId(),
                request.getDestinationAccountId(),
                request.getAmount(),
                request.getOperationDate(),
                request.getDescription()
        );
    }

    // ------------------------
    // Cancel or Confirm Transaction
    // (en la Parte C vamos a asegurar ownership en service/repo)
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
    // Obtener MIS transacciones
    // GET /api/transactions
    // ------------------------
    @GetMapping
    public List<TransactionResponse> getMyTransactions() {
        return transactionService.getMyTransactions();
    }

    // ------------------------
    // Obtener MIS transacciones por período
    // GET /api/transactions/period?from=2025-12-01&to=2025-12-31
    // ------------------------
    @GetMapping("/period")
    public List<TransactionResponse> getMyTransactionsInPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return transactionService.getMyTransactionsInPeriod(from, to);
    }

    // ------------------------
    // MODIFICAR TRANSACCION (del usuario autenticado)
    // ------------------------
    @PatchMapping("/{id}")
    public TransactionResponse updatePartial(@PathVariable Long id,
                                             @RequestBody TransactionUpdateRequest req) {
        return transactionService.update(id, req);
    }
}
