package com.example.expensetracker.controller;

import com.example.expensetracker.dto.account.AccountCreateRequest;
import com.example.expensetracker.dto.account.AccountResponse;
import com.example.expensetracker.service.AccountService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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

    // GET /api/accounts?ownerId=1
    @GetMapping
    public List<AccountResponse> listByOwner(@RequestParam Long ownerId) {
        return accountService.listByOwner(ownerId);
    }

    @GetMapping("/{id}/balance")
    public BigDecimal balance(@PathVariable Long id) {
        return accountService.calculateCurrentBalance(id);
    }
}
