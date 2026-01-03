package com.example.expensetracker.service;

import com.example.expensetracker.dto.transaction.TransactionResponse;
import com.example.expensetracker.dto.transaction.TransactionUpdateRequest;
import com.example.expensetracker.enums.TransactionState;
import com.example.expensetracker.enums.TransactionType;
import com.example.expensetracker.model.*;
import com.example.expensetracker.repository.*;
import com.example.expensetracker.security.CurrentUserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class TransactionService {

    private final CurrentUserService currentUserService;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final TransactionRepository transactionRepository;

    public TransactionService(CurrentUserService currentUserService,
                              AccountRepository accountRepository,
                              CategoryRepository categoryRepository,
                              TagRepository tagRepository,
                              TransactionRepository transactionRepository) {
        this.currentUserService = currentUserService;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.transactionRepository = transactionRepository;
    }

    private TransactionResponse toResponse(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getOwner() != null ? t.getOwner().getId() : null,
                t.getType(),
                t.getState(),
                t.getAmount(),
                t.getOperationDate(),
                t.getRecordedAt(),
                t.getDescription(),
                t.getSourceAccount() != null ? t.getSourceAccount().getId() : null,
                t.getDestinationAccount() != null ? t.getDestinationAccount().getId() : null,
                t.getCategory() != null ? t.getCategory().getId() : null,
                t.getTags() != null ? t.getTags().stream().map(Tag::getId).toList() : List.of()
        );
    }

    // ---------------------------------------------------------
    //  GASTO (EXPENSE) - owner sale del token
    // ---------------------------------------------------------
    public TransactionResponse createExpense(Long sourceAccountId,
                                             Long categoryId,
                                             BigDecimal amount,
                                             LocalDate operationDate,
                                             String description,
                                             List<Long> tagIds) {

        User owner = currentUserService.get();

        Account sourceAccount = getAccountForUserOrThrow(owner, sourceAccountId);
        Category category = getCategoryForUserOrThrow(owner, categoryId);
        Set<Tag> tags = getTagsForUser(owner, tagIds);

        validateAmountPositive(amount);
        validateAccountActive(sourceAccount);
        validateCategoryActive(category);

        Transaction tx = Transaction.builder()
                .owner(owner)
                .type(TransactionType.EXPENSE)
                .state(TransactionState.CONFIRMED)
                .amount(amount)
                .sourceAccount(sourceAccount)
                .destinationAccount(null)
                .category(category)
                .tags(tags)
                .description(description)
                .operationDate(operationDate != null ? operationDate : LocalDate.now())
                .recordedAt(LocalDateTime.now())
                .build();

        validateTransactionAccounts(tx);
        // validateTransactionOwnership(tx); // opcional: ya está blindado por repos

        Transaction saved = transactionRepository.save(tx);
        return toResponse(saved);
    }

    // ---------------------------------------------------------
    //  INGRESO (INCOME)
    // ---------------------------------------------------------
    public TransactionResponse createIncome(Long destinationAccountId,
                                            Long categoryId,
                                            BigDecimal amount,
                                            LocalDate operationDate,
                                            String description,
                                            List<Long> tagIds) {

        User owner = currentUserService.get();

        Account destinationAccount = getAccountForUserOrThrow(owner, destinationAccountId);
        Category category = getCategoryForUserOrThrow(owner, categoryId);
        Set<Tag> tags = getTagsForUser(owner, tagIds);

        validateAmountPositive(amount);
        validateAccountActive(destinationAccount);
        validateCategoryActive(category);

        Transaction tx = Transaction.builder()
                .owner(owner)
                .type(TransactionType.INCOME)
                .state(TransactionState.CONFIRMED)
                .amount(amount)
                .sourceAccount(null)
                .destinationAccount(destinationAccount)
                .category(category)
                .tags(tags)
                .description(description)
                .operationDate(operationDate != null ? operationDate : LocalDate.now())
                .recordedAt(LocalDateTime.now())
                .build();

        validateTransactionAccounts(tx);
        // validateTransactionOwnership(tx); // opcional: ya está blindado por repos

        Transaction saved = transactionRepository.save(tx);
        return toResponse(saved);
    }

    // ---------------------------------------------------------
    //  TRANSFERENCIA (TRANSFER)
    // ---------------------------------------------------------
    public TransactionResponse createTransfer(Long sourceAccountId,
                                              Long destinationAccountId,
                                              BigDecimal amount,
                                              LocalDate operationDate,
                                              String description) {

        User owner = currentUserService.get();

        Account sourceAccount = getAccountForUserOrThrow(owner, sourceAccountId);
        Account destinationAccount = getAccountForUserOrThrow(owner, destinationAccountId);

        validateAmountPositive(amount);
        validateAccountActive(sourceAccount);
        validateAccountActive(destinationAccount);

        if (sourceAccount.getId().equals(destinationAccount.getId())) {
            throw new IllegalArgumentException("Source and destination account must be different");
        }

        Transaction tx = Transaction.builder()
                .owner(owner)
                .type(TransactionType.TRANSFER)
                .state(TransactionState.CONFIRMED)
                .amount(amount)
                .sourceAccount(sourceAccount)
                .destinationAccount(destinationAccount)
                .category(null)
                .tags(new HashSet<>())
                .description(description)
                .operationDate(operationDate != null ? operationDate : LocalDate.now())
                .recordedAt(LocalDateTime.now())
                .build();

        validateTransactionAccounts(tx);
        // validateTransactionOwnership(tx); // opcional: ya está blindado por repos

        Transaction saved = transactionRepository.save(tx);
        return toResponse(saved);
    }

    // ---------------------------------------------------------
    //  CANCEL / CONFIRM (con ownership)
    // ---------------------------------------------------------
    public TransactionResponse cancel(Long transactionId) {
        Long ownerId = currentUserService.getId();

        Transaction tx = transactionRepository.findByIdAndOwnerId(transactionId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));

        if (tx.getState() != TransactionState.CANCELED) {
            tx.setState(TransactionState.CANCELED);
            transactionRepository.save(tx);
        }

        return toResponse(tx);
    }

    public TransactionResponse confirm(Long transactionId) {
        Long ownerId = currentUserService.getId();

        Transaction tx = transactionRepository.findByIdAndOwnerId(transactionId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));

        if (tx.getState() == TransactionState.CANCELED) {
            throw new IllegalArgumentException("Canceled transaction cannot be confirmed");
        }

        tx.setState(TransactionState.CONFIRMED);
        transactionRepository.save(tx);

        return toResponse(tx);
    }

    // ---------------------------------------------------------
    //  UPDATE (con ownership + category/tags del usuario)
    // ---------------------------------------------------------
    public TransactionResponse update(Long id, TransactionUpdateRequest req) {
        User owner = currentUserService.get();

        Transaction tx = transactionRepository.findByIdAndOwnerId(id, owner.getId())
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + id));

        if (tx.getState() == TransactionState.CANCELED) {
            throw new IllegalStateException("Canceled transactions cannot be edited");
        }

        if (req.getDescription() != null) {
            String d = req.getDescription().trim();
            if (d.isBlank()) throw new IllegalArgumentException("description cannot be blank");
            tx.setDescription(d);
        }

        if (req.getOperationDate() != null) {
            tx.setOperationDate(req.getOperationDate());
        }

        if (req.getCategoryId() != null) {
            Category cat = getCategoryForUserOrThrow(owner, req.getCategoryId());
            tx.setCategory(cat);
        }

        // tagIds: null => no tocar, [] => limpiar
        if (req.getTagIds() != null) {
            tx.getTags().clear();

            if (!req.getTagIds().isEmpty()) {
                Set<Tag> tags = getTagsForUser(owner, req.getTagIds());
                tx.getTags().addAll(tags);
            }
        }

        Transaction saved = transactionRepository.save(tx);
        return toResponse(saved);
    }

    // ---------------------------------------------------------
    //  LECTURA / CONSULTA (mis transacciones)
    // ---------------------------------------------------------
    public List<TransactionResponse> getMyTransactions() {
        User owner = currentUserService.get();
        return transactionRepository.findAllByOwner(owner)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<TransactionResponse> getMyTransactionsInPeriod(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("from and to are required");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("from must be <= to");
        }

        User owner = currentUserService.get();

        return transactionRepository.findAllByOwnerAndOperationDateBetween(owner, from, to)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // (usado por AccountService) listado por cuenta del usuario
    public List<TransactionResponse> listForAccount(Long accountId, Integer limit,
                                                    LocalDate from, LocalDate to) {

        User owner = currentUserService.get();

        int pageSize = (limit == null || limit <= 0) ? 20 : Math.min(limit, 200);

        var pageable = PageRequest.of(
                0,
                pageSize,
                Sort.by(Sort.Direction.DESC, "operationDate").and(Sort.by(Sort.Direction.DESC, "id"))
        );

        boolean hasPeriod = (from != null || to != null);

        if (hasPeriod) {
            if (from == null || to == null) {
                throw new IllegalArgumentException("from and to must be both provided");
            }
            if (from.isAfter(to)) {
                throw new IllegalArgumentException("from must be <= to");
            }

            return transactionRepository
                    .findByOwnerAndOperationDateBetweenAndSourceAccount_IdOrOwnerAndOperationDateBetweenAndDestinationAccount_Id(
                            owner, from, to, accountId,
                            owner, from, to, accountId,
                            pageable
                    )
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }

        return transactionRepository
                .findByOwnerAndSourceAccount_IdOrOwnerAndDestinationAccount_Id(
                        owner, accountId,
                        owner, accountId,
                        pageable
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ---------------------------------------------------------
    //  HELPERS / VALIDACIONES PRIVADAS (blindados por ownerId)
    // ---------------------------------------------------------

    private Account getAccountForUserOrThrow(User owner, Long accountId) {
        if (accountId == null) {
            throw new IllegalArgumentException("accountId is required");
        }

        // 🔒 Solo devuelve la cuenta si pertenece al usuario autenticado
        return accountRepository.findByIdAndOwnerId(accountId, owner.getId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));
    }

    private Category getCategoryForUserOrThrow(User owner, Long categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("categoryId is required");
        }

        // 🔒 Solo devuelve la categoría si pertenece al usuario autenticado
        return categoryRepository.findByIdAndOwnerId(categoryId, owner.getId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));
    }

    private Set<Tag> getTagsForUser(User owner, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new HashSet<>();
        }

        // 🔒 Trae únicamente tags del usuario
        List<Tag> tags = tagRepository.findAllByIdInAndOwnerId(tagIds, owner.getId());

        // Si te pasaron IDs que no existen o no son del usuario → rechazamos
        if (tags.size() != tagIds.size()) {
            throw new IllegalArgumentException("Some tagIds do not exist (or do not belong to user)");
        }

        return new HashSet<>(tags);
    }

    private void validateAmountPositive(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }

    private void validateAccountActive(Account account) {
        if (account.getActive() != null && !account.getActive()) {
            throw new IllegalArgumentException("Account is not active");
        }
    }

    private void validateCategoryActive(Category category) {
        if (category.getActive() != null && !category.getActive()) {
            throw new IllegalArgumentException("Category is not active");
        }
    }

    private void validateTransactionAccounts(Transaction tx) {
        if (tx.isExpense()) {
            if (tx.getSourceAccount() == null) {
                throw new IllegalArgumentException("Expense must have a source account");
            }
            if (tx.getDestinationAccount() != null) {
                throw new IllegalArgumentException("Expense must NOT have a destination account");
            }
            if (tx.getCategory() == null) {
                throw new IllegalArgumentException("Expense must have a category");
            }
        } else if (tx.isIncome()) {
            if (tx.getDestinationAccount() == null) {
                throw new IllegalArgumentException("Income must have a destination account");
            }
            if (tx.getSourceAccount() != null) {
                throw new IllegalArgumentException("Income must NOT have a source account");
            }
            if (tx.getCategory() == null) {
                throw new IllegalArgumentException("Income must have a category");
            }
        } else if (tx.isTransfer()) {
            if (tx.getSourceAccount() == null || tx.getDestinationAccount() == null) {
                throw new IllegalArgumentException("Transfer must have both source and destination accounts");
            }
            if (tx.getSourceAccount().getId().equals(tx.getDestinationAccount().getId())) {
                throw new IllegalArgumentException("Source and destination account must be different");
            }
            if (tx.getCategory() != null) {
                throw new IllegalArgumentException("Transfer must NOT have a category");
            }
            if (tx.getTags() != null && !tx.getTags().isEmpty()) {
                throw new IllegalArgumentException("Transfer must NOT have tags");
            }
        } else {
            throw new IllegalArgumentException("Unknown transaction type");
        }
    }
}
