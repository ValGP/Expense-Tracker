package com.example.expensetracker.controller;

import com.example.expensetracker.dto.account.AccountCreateRequest;
import com.example.expensetracker.dto.account.AccountUpdateRequest;
import com.example.expensetracker.dto.account.AccountResponse;
import com.example.expensetracker.dto.account.AccountBalanceResponse;
import com.example.expensetracker.dto.account.AccountSummaryResponse;
import com.example.expensetracker.dto.account.AccountDetailResponse;

import com.example.expensetracker.service.AccountService;

import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.List;
import java.time.LocalDate;

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

    // GET /api/accounts?ownerId=1
    @GetMapping
    public List<AccountSummaryResponse> listByOwner(
            @RequestParam Long ownerId,
            @RequestParam(required = false) Boolean activeOnly
    ) {
        return accountService.listByOwnerWithBalance(ownerId, activeOnly);
    }

    @GetMapping("/{id}")
    public AccountDetailResponse detail(
            @PathVariable Long id,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return accountService.getDetail(id, limit, from, to);
    }

    @GetMapping("/{id}/balance")
    public AccountBalanceResponse balance(@PathVariable Long id) {
        return accountService.getBalance(id);
    }

    // PATCH Update
    @PatchMapping("/{id}")
    public AccountResponse update(@PathVariable Long id, @RequestBody AccountUpdateRequest request) {
        return accountService.update(id, request);
    }


}
