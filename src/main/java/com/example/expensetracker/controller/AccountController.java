package com.example.expensetracker.controller;

import com.example.expensetracker.dto.account.*;
import com.example.expensetracker.service.AccountService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    // POST /api/accounts
    @PostMapping
    public AccountResponse create(@RequestBody AccountCreateRequest request) {
        return accountService.create(request);
    }

    // GET /api/accounts?activeOnly=true
    @GetMapping
    public List<AccountSummaryResponse> listMyAccounts(
            @RequestParam(required = false) Boolean activeOnly
    ) {
        return accountService.listMyAccountsWithBalance(activeOnly);
    }

    // GET /api/accounts/{id}?limit=...&from=...&to=...
    @GetMapping("/{id}")
    public AccountDetailResponse detail(
            @PathVariable Long id,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return accountService.getDetail(id, limit, from, to);
    }

    // GET /api/accounts/{id}/balance
    @GetMapping("/{id}/balance")
    public AccountBalanceResponse balance(@PathVariable Long id) {
        return accountService.getBalance(id);
    }

    // PATCH /api/accounts/{id}
    @PatchMapping("/{id}")
    public AccountResponse update(@PathVariable Long id, @RequestBody AccountUpdateRequest request) {
        return accountService.update(id, request);
    }
}
