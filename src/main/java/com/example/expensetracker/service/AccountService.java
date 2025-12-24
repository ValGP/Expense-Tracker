package com.example.expensetracker.service;

import com.example.expensetracker.dto.account.AccountCreateRequest;
import com.example.expensetracker.dto.account.AccountUpdateRequest;
import com.example.expensetracker.dto.account.AccountResponse;
import com.example.expensetracker.dto.account.AccountBalanceResponse;
import com.example.expensetracker.dto.account.AccountSummaryResponse;
import com.example.expensetracker.dto.account.AccountDetailResponse;
import com.example.expensetracker.dto.transaction.TransactionResponse;

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
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class AccountService {

    private final UserRepository userRepository;
    private final CurrencyRepository currencyRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;

    public AccountService(UserRepository userRepository,
                          CurrencyRepository currencyRepository,
                          AccountRepository accountRepository,
                          TransactionRepository transactionRepository,
                          TransactionService transactionService) {
        this.userRepository = userRepository;
        this.currencyRepository = currencyRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.transactionService = transactionService;
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

    public AccountBalanceResponse getBalance(Long accountId) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        BigDecimal balance = calculateCurrentBalance(accountId);

        return new AccountBalanceResponse(
                account.getId(),
                account.getOwner().getId(),
                account.getName(),
                account.getCurrency() != null ? account.getCurrency().getCode() : null,
                balance
        );
    }

    public List<AccountSummaryResponse> listByOwnerWithBalance(Long ownerId, Boolean activeOnly) {

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + ownerId));

        return accountRepository.findByOwner(owner).stream()
                .filter(a -> activeOnly == null || !activeOnly || Boolean.TRUE.equals(a.getActive()))
                .map(a -> new AccountSummaryResponse(
                        a.getId(),
                        owner.getId(),
                        a.getName(),
                        a.getType(),
                        a.getCurrency() != null ? a.getCurrency().getCode() : null,
                        calculateCurrentBalance(a.getId()),
                        a.getActive()
                ))
                .toList();
    }


    //UPDATE ACCOUNT
    public AccountResponse update(Long accountId, AccountUpdateRequest req) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        // name
        if (req.getName() != null) {
            String newName = req.getName().trim();
            if (newName.isBlank()) {
                throw new IllegalArgumentException("name cannot be blank");
            }
            // evitar duplicados para el mismo owner
            if (!newName.equalsIgnoreCase(account.getName())
                    && accountRepository.existsByOwnerAndName(account.getOwner(), newName)) {
                throw new IllegalArgumentException("Account with that name already exists for this user");
            }
            account.setName(newName);
        }

        // type
        if (req.getType() != null) {
            account.setType(req.getType());
        }

        // currency
        if (req.getCurrencyCode() != null) {
            String code = req.getCurrencyCode().trim();
            if (code.isBlank()) {
                throw new IllegalArgumentException("currencyCode cannot be blank");
            }
            Currency currency = currencyRepository.findById(code)
                    .orElseThrow(() -> new IllegalArgumentException("Currency not found: " + code));
            account.setCurrency(currency);
        }

        // active
        if (req.getActive() != null) {
            account.setActive(req.getActive());
        }

        Account saved = accountRepository.save(account);
        return toResponse(saved);
    }

    //GET DETAIL
    public AccountDetailResponse getDetail(Long accountId, Integer limit, LocalDate from, LocalDate to) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        Long ownerId = account.getOwner().getId();

        AccountSummaryResponse summary = new AccountSummaryResponse(
                account.getId(),
                ownerId,
                account.getName(),
                account.getType(),
                account.getCurrency() != null ? account.getCurrency().getCode() : null,
                calculateCurrentBalance(account.getId()),
                account.getActive()
        );

        List<TransactionResponse> txs =
                transactionService.listForAccount(ownerId, accountId, limit, from, to);

        return new AccountDetailResponse(summary, txs);
    }
}


