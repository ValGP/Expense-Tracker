package com.example.expensetracker.service;

import com.example.expensetracker.dto.account.AccountCreateRequest;
import com.example.expensetracker.dto.account.AccountResponse;
import com.example.expensetracker.model.Account;
import com.example.expensetracker.model.Currency;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.AccountRepository;
import com.example.expensetracker.repository.CurrencyRepository;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.repository.TransactionRepository;
import com.example.expensetracker.enums.TransactionState;
import com.example.expensetracker.enums.TransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AccountService {

    private final UserRepository userRepository;
    private final CurrencyRepository currencyRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;


    public AccountService(UserRepository userRepository,
                          CurrencyRepository currencyRepository,
                          AccountRepository accountRepository,
                          TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.currencyRepository = currencyRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;

    }

    public AccountResponse create(AccountCreateRequest req) {

        if (req.getOwnerId() == null) {
            throw new IllegalArgumentException("ownerId is required");
        }
        if (req.getName() == null || req.getName().isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (req.getType() == null) {
            throw new IllegalArgumentException("type is required");
        }

        User owner = userRepository.findById(req.getOwnerId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + req.getOwnerId()));

        // moneda: si no viene, usar la default del user (si existe) o ARS
        Currency currency;
        if (req.getCurrencyCode() != null && !req.getCurrencyCode().isBlank()) {
            currency = currencyRepository.findById(req.getCurrencyCode())
                    .orElseThrow(() -> new IllegalArgumentException("Currency not found: " + req.getCurrencyCode()));
        } else if (owner.getDefaultCurrency() != null) {
            currency = owner.getDefaultCurrency();
        } else {
            currency = currencyRepository.findById("ARS")
                    .orElseThrow(() -> new IllegalArgumentException("Default currency ARS not found"));
        }

        BigDecimal initialBalance = req.getInitialBalance() != null ? req.getInitialBalance() : BigDecimal.ZERO;

        // (opcional) evitar duplicados por nombre para ese usuario
        if (accountRepository.existsByOwnerAndName(owner, req.getName())) {
            throw new IllegalArgumentException("Account with that name already exists for this user");
        }

        Account account = Account.builder()
                .owner(owner)
                .name(req.getName().trim())
                .type(req.getType())
                .currency(currency)
                .initialBalance(initialBalance)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        Account saved = accountRepository.save(account);

        return toResponse(saved);
    }

    public List<AccountResponse> listByOwner(Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + ownerId));

        return accountRepository.findByOwner(owner)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private AccountResponse toResponse(Account a) {
        return new AccountResponse(
                a.getId(),
                a.getName(),
                a.getType(),
                a.getCurrency() != null ? a.getCurrency().getCode() : null,
                a.getInitialBalance(),
                a.getActive()
        );
    }

    public BigDecimal calculateCurrentBalance(Long accountId) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        BigDecimal initial = account.getInitialBalance() != null ? account.getInitialBalance() : BigDecimal.ZERO;

        BigDecimal expensesOut = transactionRepository.sumByTypeAndStateAndSourceAccount(
                TransactionType.EXPENSE,
                TransactionState.CONFIRMED,
                accountId
        );

        BigDecimal incomesIn = transactionRepository.sumByTypeAndStateAndDestinationAccount(
                TransactionType.INCOME,
                TransactionState.CONFIRMED,
                accountId
        );

        BigDecimal transfersOut = transactionRepository.sumByTypeAndStateAndSourceAccount(
                TransactionType.TRANSFER,
                TransactionState.CONFIRMED,
                accountId
        );

        BigDecimal transfersIn = transactionRepository.sumByTypeAndStateAndDestinationAccount(
                TransactionType.TRANSFER,
                TransactionState.CONFIRMED,
                accountId
        );

        return initial
                .add(incomesIn)
                .add(transfersIn)
                .subtract(expensesOut)
                .subtract(transfersOut);
    }
}
