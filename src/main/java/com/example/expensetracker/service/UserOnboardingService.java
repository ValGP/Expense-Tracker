package com.example.expensetracker.service;

import com.example.expensetracker.enums.AccountType;
import com.example.expensetracker.model.*;
import com.example.expensetracker.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserOnboardingService {

    private final CurrencyRepository currencyRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    public UserOnboardingService(CurrencyRepository currencyRepository,
                                 AccountRepository accountRepository,
                                 CategoryRepository categoryRepository) {
        this.currencyRepository = currencyRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public void seedDefaultsFor(User user) {
        Currency ars = ensureArsCurrency();

        // Set default currency if empty
        if (user.getDefaultCurrency() == null) {
            user.setDefaultCurrency(ars);
        }

        createDefaultAccountsIfMissing(user, ars);
        createDefaultCategoriesIfMissing(user);
    }

    private Currency ensureArsCurrency() {
        return currencyRepository.findById("ARS")
                .orElseGet(() -> currencyRepository.save(
                        Currency.builder()
                                .code("ARS")
                                .name("Argentine Peso")
                                .symbol("$")
                                .decimalDigits(2)
                                .exchangeRateToBase(BigDecimal.ONE)
                                .build()
                ));
    }

    private void createDefaultAccountsIfMissing(User user, Currency currency) {
        if (!accountRepository.existsByOwnerAndName(user, "Cash Wallet")) {
            accountRepository.save(Account.builder()
                    .owner(user)
                    .name("Cash Wallet")
                    .type(AccountType.CASH)
                    .currency(currency)
                    .initialBalance(BigDecimal.ZERO)
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .build());
        }

        if (!accountRepository.existsByOwnerAndName(user, "Bank Account")) {
            accountRepository.save(Account.builder()
                    .owner(user)
                    .name("Bank Account")
                    .type(AccountType.BANK)
                    .currency(currency)
                    .initialBalance(BigDecimal.ZERO)
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .build());
        }
    }

    private void createDefaultCategoriesIfMissing(User user) {
        // nombre, descripción, color
        var defaults = List.of(
                new Cat("Food", "Restaurants, delivery, groceries", "#FF8800"),
                new Cat("Transport", "Fuel, taxi, bus, rides", "#3B82F6"),
                new Cat("Bills", "Services, rent, utilities", "#A855F7"),
                new Cat("Entertainment", "Movies, games, fun", "#F43F5E"),
                new Cat("Health", "Pharmacy, doctor, gym", "#22C55E"),
                new Cat("Shopping", "Clothes, gadgets, stuff", "#F59E0B"),
                new Cat("Salary", "Main income", "#10B981"),
                new Cat("Other", "Miscellaneous", "#64748B")
        );

        for (Cat c : defaults) {
            if (!categoryRepository.existsByOwnerAndNameIgnoreCase(user, c.name)) {
                categoryRepository.save(Category.builder()
                        .owner(user)
                        .name(c.name)
                        .description(c.desc)
                        .colorHex(c.color)
                        .active(true)
                        .build());
            }
        }
    }

    private record Cat(String name, String desc, String color) {}
}
