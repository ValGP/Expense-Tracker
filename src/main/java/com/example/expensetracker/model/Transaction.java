package com.example.expensetracker.model;

import com.example.expensetracker.enums.TransactionState;
import com.example.expensetracker.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // dueño
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    private TransactionState state;

    private BigDecimal amount;

    private LocalDate operationDate;
    private LocalDateTime recordedAt;

    private String description;
    private String externalReference;

    // cuenta origen
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_account_id")
    private Account sourceAccount;

    // cuenta destino
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_account_id")
    private Account destinationAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToMany
    @JoinTable(
            name = "transaction_tags",
            joinColumns = @JoinColumn(name = "transaction_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    @PrePersist
    public void prePersist() {
        if (recordedAt == null) {
            recordedAt = LocalDateTime.now();
        }
        if (operationDate == null) {
            operationDate = recordedAt.toLocalDate();
        }
        if (state == null) {
            state = TransactionState.PENDING;
        }
    }

    public void confirm() {
        this.state = TransactionState.CONFIRMED;
    }

    public void cancel() {
        this.state = TransactionState.CANCELED;
    }

    public void addTag(Tag tag) {
        tags.add(tag);
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
    }

    public boolean isExpense() {
        return TransactionType.EXPENSE.equals(type);
    }

    public boolean isIncome() {
        return TransactionType.INCOME.equals(type);
    }

    public boolean isTransfer() {
        return TransactionType.TRANSFER.equals(type);
    }
}
