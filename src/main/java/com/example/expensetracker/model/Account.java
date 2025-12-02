package com.example.expensetracker.model;

import com.example.expensetracker.enums.AccountType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id")
    private User owner;

    private String name;

    @Enumerated(EnumType.STRING)
    private AccountType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_code")
    private Currency currency;

    private BigDecimal initialBalance;

    private Boolean active;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (active == null) {
            active = true;
        }
    }

    public void rename(String newName) {
        this.name = newName;
    }

    public void changeType(AccountType newType) {
        this.type = newType;
    }

    public void changeCurrency(Currency newCurrency) {
        this.currency = newCurrency;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }
}
