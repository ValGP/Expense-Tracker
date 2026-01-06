package com.example.expensetracker.service;

import com.example.expensetracker.dto.account.*;
import com.example.expensetracker.dto.transaction.TransactionResponse;
import com.example.expensetracker.enums.TransactionState;
import com.example.expensetracker.enums.TransactionType;
import com.example.expensetracker.model.Account;
import com.example.expensetracker.model.Currency;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.*;
import com.example.expensetracker.security.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AccountService {

    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final CurrencyRepository currencyRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;

    public AccountService(CurrentUserService currentUserService,
                          UserRepository userRepository,
                          CurrencyRepository currencyRepository,
                          AccountRepository accountRepository,
                          TransactionRepository transactionRepository,
                          TransactionService transactionService) {
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
        this.currencyRepository = currencyRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.transactionService = transactionService;
    }

    public AccountResponse create(AccountCreateRequest req) {
        if (req.getName() == null || req.getName().isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (req.getType() == null) {
            throw new IllegalArgumentException("type is required");
        }

        Long myId = currentUserService.getId();
        User owner = userRepository.findById(myId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + myId));

        // moneda: si no viene, usar default del user o ARS
        Currency currency;
        if (req.getCurrencyCode() != null && !req.getCurrencyCode().isBlank()) {
            currency = currencyRepository.findById(req.getCurrencyCode().trim())
                    .orElseThrow(() -> new IllegalArgumentException("Currency not found: " + req.getCurrencyCode()));
        } else if (owner.getDefaultCurrency() != null) {
            currency = owner.getDefaultCurrency();
        } else {
            currency = currencyRepository.findById("ARS")
                    .orElseThrow(() -> new IllegalArgumentException("Default currency ARS not found"));
        }

        BigDecimal initialBalance = req.getInitialBalance() != null ? req.getInitialBalance() : BigDecimal.ZERO;

        if (accountRepository.existsByOwnerAndName(owner, req.getName().trim())) {
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

    public List<AccountSummaryResponse> listMyAccountsWithBalance(Boolean activeOnly) {
        Long myId = currentUserService.getId();
        User owner = userRepository.findById(myId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + myId));

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

    public AccountResponse update(Long accountId, AccountUpdateRequest req) {
        Long myId = currentUserService.getId();

        Account account = accountRepository.findByIdAndOwnerId(accountId, myId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        // name
        if (req.getName() != null) {
            String newName = req.getName().trim();
            if (newName.isBlank()) {
                throw new IllegalArgumentException("name cannot be blank");
            }
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

    public AccountBalanceResponse getBalance(Long accountId) {
        Long myId = currentUserService.getId();

        Account account = accountRepository.findByIdAndOwnerId(accountId, myId)
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

    public AccountDetailResponse getDetail(Long accountId, Integer limit, LocalDate from, LocalDate to) {
        Long myId = currentUserService.getId();

        Account account = accountRepository.findByIdAndOwnerId(accountId, myId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        AccountSummaryResponse summary = new AccountSummaryResponse(
                account.getId(),
                account.getOwner().getId(),
                account.getName(),
                account.getType(),
                account.getCurrency() != null ? account.getCurrency().getCode() : null,
                calculateCurrentBalance(account.getId()),
                account.getActive()
        );

        // TransactionService ya está ownership-aware por CurrentUserService
        List<TransactionResponse> txs = transactionService.listForAccount(accountId, limit, from, to);

        return new AccountDetailResponse(summary, txs);
    }

    public BigDecimal calculateCurrentBalance(Long accountId) {
        Long myId = currentUserService.getId();

        // 🔒 clave: la cuenta debe ser del usuario
        accountRepository.findByIdAndOwnerId(accountId, myId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        BigDecimal expensesOut = transactionRepository.sumByTypeAndStateAndSourceAccount(
                TransactionType.EXPENSE, TransactionState.CONFIRMED, accountId
        );

        BigDecimal incomesIn = transactionRepository.sumByTypeAndStateAndDestinationAccount(
                TransactionType.INCOME, TransactionState.CONFIRMED, accountId
        );

        BigDecimal transfersOut = transactionRepository.sumByTypeAndStateAndSourceAccount(
                TransactionType.TRANSFER, TransactionState.CONFIRMED, accountId
        );

        BigDecimal transfersIn = transactionRepository.sumByTypeAndStateAndDestinationAccount(
                TransactionType.TRANSFER, TransactionState.CONFIRMED, accountId
        );

        Account account = accountRepository.findByIdAndOwnerId(accountId, myId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        BigDecimal initial = account.getInitialBalance() != null ? account.getInitialBalance() : BigDecimal.ZERO;

        return initial
                .add(incomesIn)
                .add(transfersIn)
                .subtract(expensesOut)
                .subtract(transfersOut);
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
}
