package com.example.expensetracker.service;

import com.example.expensetracker.enums.TransactionState;
import com.example.expensetracker.enums.TransactionType;
import com.example.expensetracker.model.*;
import com.example.expensetracker.repository.*;
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

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final TransactionRepository transactionRepository;

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
    public Transaction createExpense(Long ownerId,
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
                .state(TransactionState.PENDING)
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

        tx.confirm(); // Por ahora confirmamos directamente

        return transactionRepository.save(tx);
    }

    // ---------------------------------------------------------
    //  INGRESO (INCOME)
    // ---------------------------------------------------------
    public Transaction createIncome(Long ownerId,
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
                .state(TransactionState.PENDING)
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

        tx.confirm();

        return transactionRepository.save(tx);
    }

    // ---------------------------------------------------------
    //  TRANSFERENCIA (TRANSFER)
    // ---------------------------------------------------------
    public Transaction createTransfer(Long ownerId,
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
                .state(TransactionState.PENDING)
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

        tx.confirm();

        return transactionRepository.save(tx);
    }

    // ---------------------------------------------------------
    //  LECTURA / CONSULTA
    // ---------------------------------------------------------

    public List<Transaction> getTransactionsForUser(Long ownerId) {
        User owner = getUserOrThrow(ownerId);
        return transactionRepository.findByOwner(owner);
    }

    public List<Transaction> getTransactionsForUserInPeriod(Long ownerId,
                                                            LocalDate from,
                                                            LocalDate to) {
        User owner = getUserOrThrow(ownerId);
        return transactionRepository.findByOwnerAndOperationDateBetween(owner, from, to);
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
        } else if (tx.isIncome()) {
            if (tx.getDestinationAccount() == null) {
                throw new IllegalArgumentException("Income must have a destination account");
            }
        } else if (tx.isTransfer()) {
            if (tx.getSourceAccount() == null || tx.getDestinationAccount() == null) {
                throw new IllegalArgumentException("Transfer must have both source and destination accounts");
            }
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
