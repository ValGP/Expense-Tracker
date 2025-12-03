package com.example.expensetracker;

import com.example.expensetracker.enums.AccountType;
import com.example.expensetracker.model.*;
import com.example.expensetracker.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@SpringBootApplication
public class ExpenseTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExpenseTrackerApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(
            UserRepository userRepository,
            CurrencyRepository currencyRepository,
            AccountRepository accountRepository,
            CategoryRepository categoryRepository,
            TagRepository tagRepository
    ) {
        return args -> {

            // CURRENCY ARS
            Currency ars = currencyRepository.findById("ARS")
                    .orElseGet(() -> {
                        Currency c = Currency.builder()
                                .code("ARS")
                                .name("Argentine Peso")
                                .symbol("$")
                                .decimalDigits(2)
                                .exchangeRateToBase(BigDecimal.ONE)
                                .build();
                        return currencyRepository.save(c);
                    });

            // USER DEMO
            User demoUser = userRepository.findByEmail("demo@example.com")
                    .orElseGet(() -> {
                        User u = User.builder()
                                .name("Demo User")
                                .email("demo@example.com")
                                .passwordHash("hashed-password")
                                .defaultCurrency(ars)
                                .createdAt(LocalDateTime.now())
                                .active(true)
                                .build();
                        return userRepository.save(u);
                    });

            // ACCOUNTS
            if (accountRepository.count() == 0) {

                Account cash = Account.builder()
                        .owner(demoUser)
                        .name("Cash Wallet")
                        .type(AccountType.CASH)
                        .currency(ars)
                        .initialBalance(new BigDecimal("20000"))
                        .active(true)
                        .createdAt(LocalDateTime.now())
                        .build();

                Account bank = Account.builder()
                        .owner(demoUser)
                        .name("Bank Account")
                        .type(AccountType.BANK)
                        .currency(ars)
                        .initialBalance(new BigDecimal("50000"))
                        .active(true)
                        .createdAt(LocalDateTime.now())
                        .build();

                accountRepository.save(cash);
                accountRepository.save(bank);
            }

            // CATEGORIES
            if (categoryRepository.count() == 0) {
                Category food = Category.builder()
                        .owner(demoUser)
                        .name("Food")
                        .description("Restaurants, delivery, groceries")
                        .colorHex("#FF8800")
                        .active(true)
                        .build();

                categoryRepository.save(food);
            }

            // TAGS
            if (tagRepository.count() == 0) {
                Tag utn = Tag.builder()
                        .owner(demoUser)
                        .name("UTN")
                        .active(true)
                        .build();

                Tag friends = Tag.builder()
                        .owner(demoUser)
                        .name("Friends")
                        .active(true)
                        .build();

                tagRepository.save(utn);
                tagRepository.save(friends);
            }
        };
    }
}
