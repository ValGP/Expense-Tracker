package com.example.expensetracker.service;

import com.example.expensetracker.enums.TransactionState;
import com.example.expensetracker.enums.TransactionType;
import com.example.expensetracker.model.*;
import com.example.expensetracker.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.example.expensetracker.dto.transaction.TransactionResponse;
import com.example.expensetracker.dto.transaction.TransactionUpdateRequest;
import com.example.expensetracker.model.Tag;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class TransactionService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final TransactionRepository transactionRepository;

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
                t.getTags() != null
                        ? t.getTags().stream().map(Tag::getId).toList()
                        : List.of()
        );
    }

    public TransactionService(UserRepository userRepository,
                              AccountRepository accountRepository,
                              CategoryRepository categoryRepository,
                              TagRepository tagRepository,
                              TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.transactionRepository = transactionRepository;
    }

    // ---------------------------------------------------------
    //  GASTO (EXPENSE)
    // ---------------------------------------------------------
    public TransactionResponse createExpense(Long ownerId,
                                     Long sourceAccountId,
                                     Long categoryId,
                                     BigDecimal amount,
                                     LocalDate operationDate,
                                     String description,
                                     List<Long> tagIds) {

        User owner = getUserOrThrow(ownerId);
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

        // Validaciones de consistencia de la propia transacción
        validateTransactionAccounts(tx);
        validateTransactionOwnership(tx);

        // En este punto podríamos aplicar reglas extra (límites, saldo, etc.)



        //return transactionRepository.save(tx);

        Transaction saved = transactionRepository.save(tx);
        return toResponse(saved);
    }

    // ---------------------------------------------------------
    //  INGRESO (INCOME)
    // ---------------------------------------------------------
    public TransactionResponse createIncome(Long ownerId,
                                    Long destinationAccountId,
                                    Long categoryId,
                                    BigDecimal amount,
                                    LocalDate operationDate,
                                    String description,
                                    List<Long> tagIds) {

        User owner = getUserOrThrow(ownerId);
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
        validateTransactionOwnership(tx);


        Transaction saved = transactionRepository.save(tx);
        return toResponse(saved);
    }

    // ---------------------------------------------------------
    //  TRANSFERENCIA (TRANSFER)
    // ---------------------------------------------------------
    public TransactionResponse createTransfer(Long ownerId,
                                      Long sourceAccountId,
                                      Long destinationAccountId,
                                      BigDecimal amount,
                                      LocalDate operationDate,
                                      String description) {

        User owner = getUserOrThrow(ownerId);
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
        validateTransactionOwnership(tx);


        Transaction saved = transactionRepository.save(tx);
        return toResponse(saved);
    }

    // ---------------------------------------------------------
    //  CANCEL / CONFIRM
    // ---------------------------------------------------------

    public TransactionResponse cancel(Long transactionId) {

        Transaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));

        // idempotente: si ya está cancelada, devolvés igual
        if (tx.getState() != TransactionState.CANCELED) {
            tx.setState(TransactionState.CANCELED);
            transactionRepository.save(tx);
        }

        return toResponse(tx);
    }

    @Transactional
    public TransactionResponse confirm(Long transactionId) {

        Transaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));

        // opcional: si estaba cancelada, ¿permitís re-confirmar?
        // yo diría que NO, para no hacer lío:
        if (tx.getState() == TransactionState.CANCELED) {
            throw new IllegalArgumentException("Canceled transaction cannot be confirmed");
        }

        tx.setState(TransactionState.CONFIRMED);
        transactionRepository.save(tx);

        return toResponse(tx);
    }

    // ---------------------------------------------------------
    //  UPDATE
    // ---------------------------------------------------------

    public TransactionResponse update(Long id, TransactionUpdateRequest req) {

        Transaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + id));

        if (tx.getState() == TransactionState.CANCELED) {
            throw new IllegalStateException("Canceled transactions cannot be edited");
        }

        // description
        if (req.getDescription() != null) {
            String d = req.getDescription().trim();
            if (d.isBlank()) throw new IllegalArgumentException("description cannot be blank");
            tx.setDescription(d);
        }

        // operationDate
        if (req.getOperationDate() != null) {
            tx.setOperationDate(req.getOperationDate());
        }

        // categoryId
        if (req.getCategoryId() != null) {
            Category cat = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found: " + req.getCategoryId()));
            tx.setCategory(cat);
        }

        // tagIds: null no cambia, [] limpia
        if (req.getTagIds() != null) {
            tx.getTags().clear();

            if (!req.getTagIds().isEmpty()) {
                List<Tag> tags = tagRepository.findAllById(req.getTagIds());

                // opcional pero recomendable para evitar ids inválidos silenciosos
                if (tags.size() != req.getTagIds().size()) {
                    throw new IllegalArgumentException("Some tags not found");
                }

                tx.getTags().addAll(new HashSet<>(tags));
            }
        }

        // No es obligatorio llamar save() si tx está managed en la transacción,
        // pero podés dejarlo explícito:
        Transaction saved = transactionRepository.save(tx);

        return toResponse(saved);
    }

    // ---------------------------------------------------------
    //  LECTURA / CONSULTA
    // ---------------------------------------------------------

    //Transacciones del Usuario

    public List<TransactionResponse> getTransactionsForUser(Long ownerId) {
        User owner = getUserOrThrow(ownerId);
        return transactionRepository.findByOwner(owner)
                                    .stream()
                                    .map(this::toResponse)
                                    .toList();
    }

    public List<TransactionResponse> getTransactionsForUserInPeriod(Long ownerId, LocalDate from, LocalDate to) {

        if (from == null || to == null) {
            throw new IllegalArgumentException("from and to are required");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("from must be <= to");
        }

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + ownerId));

        return transactionRepository.findByOwnerAndOperationDateBetween(owner, from, to)
                                    .stream()
                                    .map(this::toResponse)
                                    .toList();
    }

    //Transacciones del Usuario en una cuenta especifica

    public List<TransactionResponse> listForAccount(Long ownerId, Long accountId, Integer limit,
                                                    LocalDate from, LocalDate to) {

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + ownerId));

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
    //  HELPERS / VALIDACIONES PRIVADAS
    // ---------------------------------------------------------

    private User getUserOrThrow(Long ownerId) {
        return userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + ownerId));
    }

    private Account getAccountForUserOrThrow(User owner, Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        if (!account.getOwner().getId().equals(owner.getId())) {
            throw new IllegalArgumentException("Account does not belong to user");
        }
        return account;
    }

    private Category getCategoryForUserOrThrow(User owner, Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));

        if (!category.getOwner().getId().equals(owner.getId())) {
            throw new IllegalArgumentException("Category does not belong to user");
        }
        return category;
    }

    private Set<Tag> getTagsForUser(User owner, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new HashSet<>();
        }

        List<Tag> tags = tagRepository.findAllById(tagIds);

        // 1) si faltó alguno, error
        if (tags.size() != tagIds.size()) {
            throw new IllegalArgumentException("Some tagIds do not exist");
        }

        // 2) ownership
        Set<Tag> result = new HashSet<>();
        for (Tag tag : tags) {
            if (!tag.getOwner().getId().equals(owner.getId())) {
                throw new IllegalArgumentException("Tag does not belong to user");
            }
            result.add(tag);
        }
        return result;
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
        }

        else if (tx.isIncome()) {
            if (tx.getDestinationAccount() == null) {
                throw new IllegalArgumentException("Income must have a destination account");
            }
            if (tx.getSourceAccount() != null) {
                throw new IllegalArgumentException("Income must NOT have a source account");
            }
            if (tx.getCategory() == null) {
                throw new IllegalArgumentException("Income must have a category");
            }
        }

        else if (tx.isTransfer()) {
            if (tx.getSourceAccount() == null || tx.getDestinationAccount() == null) {
                throw new IllegalArgumentException("Transfer must have both source and destination accounts");
            }
            if (tx.getSourceAccount().getId().equals(tx.getDestinationAccount().getId())) {
                throw new IllegalArgumentException("Source and destination account must be different");
            }
            if (tx.getCategory() != null) {
                throw new IllegalArgumentException("Transfer must NOT have a category");
            }
            // tags en transfer: si querés permitirlos, sacá esta regla
            if (tx.getTags() != null && !tx.getTags().isEmpty()) {
                throw new IllegalArgumentException("Transfer must NOT have tags");
            }
        }

        else {
            throw new IllegalArgumentException("Unknown transaction type");
        }
    }


    private void validateTransactionOwnership(Transaction tx) {
        Long ownerId = tx.getOwner().getId();

        if (tx.getSourceAccount() != null &&
                !tx.getSourceAccount().getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("Source account does not belong to transaction owner");
        }

        if (tx.getDestinationAccount() != null &&
                !tx.getDestinationAccount().getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("Destination account does not belong to transaction owner");
        }

        if (tx.getCategory() != null &&
                !tx.getCategory().getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("Category does not belong to transaction owner");
        }

        if (tx.getTags() != null) {
            tx.getTags().forEach(tag -> {
                if (!tag.getOwner().getId().equals(ownerId)) {
                    throw new IllegalArgumentException("Tag does not belong to transaction owner");
                }
            });
        }
    }


}
