package com.example.expensetracker;

import com.example.expensetracker.config.JwtProperties;
import com.example.expensetracker.enums.AccountType;
import com.example.expensetracker.enums.TransactionState;
import com.example.expensetracker.enums.TransactionType;
import com.example.expensetracker.model.*;
import com.example.expensetracker.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;

@EnableConfigurationProperties(JwtProperties.class)
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
            TagRepository tagRepository,
            TransactionRepository transactionRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {

            // -------------------------
            // CURRENCY ARS
            // -------------------------
            Currency ars = currencyRepository.findById("ARS")
                    .orElseGet(() -> currencyRepository.save(
                            Currency.builder()
                                    .code("ARS")
                                    .name("Argentine Peso")
                                    .symbol("$")
                                    .decimalDigits(2)
                                    .exchangeRateToBase(BigDecimal.ONE)
                                    .build()
                    ));

            // -------------------------
            // USER DEMO
            // -------------------------
            User demoUser = userRepository.findByEmail("demo@example.com")
                    .orElseGet(() -> userRepository.save(
                            User.builder()
                                    .name("Demo User")
                                    .email("demo@example.com")
                                    .passwordHash(passwordEncoder.encode("demo12345"))
                                    .defaultCurrency(ars)
                                    .createdAt(LocalDateTime.now())
                                    .active(true)
                                    .build()
                    ));

            // -------------------------
            // ACCOUNTS (3)
            // -------------------------
            if (!accountRepository.existsByOwnerAndName(demoUser, "Cash Wallet")) {
                accountRepository.save(Account.builder()
                        .owner(demoUser)
                        .name("Cash Wallet")
                        .type(AccountType.CASH)
                        .currency(ars)
                        .initialBalance(new BigDecimal("35000"))
                        .active(true)
                        .createdAt(LocalDateTime.now())
                        .build());
            }

            if (!accountRepository.existsByOwnerAndName(demoUser, "Bank Account")) {
                accountRepository.save(Account.builder()
                        .owner(demoUser)
                        .name("Bank Account")
                        .type(AccountType.BANK)
                        .currency(ars)
                        .initialBalance(new BigDecimal("450000"))
                        .active(true)
                        .createdAt(LocalDateTime.now())
                        .build());
            }

            if (!accountRepository.existsByOwnerAndName(demoUser, "Wallet")) {
                accountRepository.save(Account.builder()
                        .owner(demoUser)
                        .name("Wallet")
                        .type(AccountType.DIGITAL_WALLET)
                        .currency(ars)
                        .initialBalance(new BigDecimal("150000"))
                        .active(true)
                        .createdAt(LocalDateTime.now())
                        .build());
            }

            Account cash = accountRepository.findByOwnerAndName(demoUser, "Cash Wallet").orElseThrow();
            Account bank = accountRepository.findByOwnerAndName(demoUser, "Bank Account").orElseThrow();
            Account wallet = accountRepository.findByOwnerAndName(demoUser, "Wallet").orElseThrow();

            // -------------------------
            // CATEGORIES (8) con colores
            // -------------------------
            ensureCategory(categoryRepository, demoUser, "Groceries", "Supermarkets & food", "#22c55e");
            ensureCategory(categoryRepository, demoUser, "Rent", "Monthly rent", "#ef4444");
            ensureCategory(categoryRepository, demoUser, "Transport", "Bus, taxi, fuel", "#3b82f6");
            ensureCategory(categoryRepository, demoUser, "Coffee", "Coffee & snacks", "#a855f7");
            ensureCategory(categoryRepository, demoUser, "Health", "Pharmacy & medical", "#f97316");
            ensureCategory(categoryRepository, demoUser, "Fun", "Movies, outings", "#14b8a6");
            ensureCategory(categoryRepository, demoUser, "Bills", "Services & subscriptions", "#64748b");
            ensureCategory(categoryRepository, demoUser, "Salary", "Income", "#0ea5e9");

            Category groceries = categoryRepository.findByOwnerAndNameIgnoreCase(demoUser, "Groceries").orElseThrow();
            Category rent      = categoryRepository.findByOwnerAndNameIgnoreCase(demoUser, "Rent").orElseThrow();
            Category transport = categoryRepository.findByOwnerAndNameIgnoreCase(demoUser, "Transport").orElseThrow();
            Category coffee    = categoryRepository.findByOwnerAndNameIgnoreCase(demoUser, "Coffee").orElseThrow();
            Category health    = categoryRepository.findByOwnerAndNameIgnoreCase(demoUser, "Health").orElseThrow();
            Category fun       = categoryRepository.findByOwnerAndNameIgnoreCase(demoUser, "Fun").orElseThrow();
            Category bills     = categoryRepository.findByOwnerAndNameIgnoreCase(demoUser, "Bills").orElseThrow();
            Category salary    = categoryRepository.findByOwnerAndNameIgnoreCase(demoUser, "Salary").orElseThrow();

            // -------------------------
            // TAGS (2)
            // -------------------------
            if (!tagRepository.existsByOwnerAndNameIgnoreCase(demoUser, "UTN")) {
                tagRepository.save(Tag.builder()
                        .owner(demoUser)
                        .name("UTN")
                        .active(true)
                        .build());
            }

            if (!tagRepository.existsByOwnerAndNameIgnoreCase(demoUser, "Friends")) {
                tagRepository.save(Tag.builder()
                        .owner(demoUser)
                        .name("Friends")
                        .active(true)
                        .build());
            }

            Tag utn = tagRepository.findByOwnerAndNameIgnoreCase(demoUser, "UTN").orElseThrow();
            Tag friends = tagRepository.findByOwnerAndNameIgnoreCase(demoUser, "Friends").orElseThrow();

            // -------------------------
            // TRANSACTIONS (solo si no hay ninguna aún)
            // (así no duplica cada run)
            // -------------------------
            if (!transactionRepository.findAllByOwner(demoUser).isEmpty()) {
                return;
            }

            LocalDate today = LocalDate.now();

            // 2 incomes para que balance se vea “vivo”
            transactionRepository.save(income(demoUser, bank, salary, "Salary", "650000", today.minusDays(3), utn));
            transactionRepository.save(income(demoUser, wallet, salary, "Freelance", "90000", today.minusDays(18)));

            // algunos gastos “manuales”
            transactionRepository.save(expense(demoUser, bank, rent, "Rent", "120000", today.minusDays(10)));
            transactionRepository.save(expense(demoUser, bank, bills, "Internet", "18000", today.minusDays(7)));
            transactionRepository.save(expense(demoUser, wallet, coffee, "Coffee", "1800", today.minusDays(1)));
            transactionRepository.save(expense(demoUser, cash, transport, "Bus card", "4500", today.minusDays(5)));
            transactionRepository.save(expense(demoUser, bank, health, "Pharmacy", "9300", today.minusDays(20)));
            transactionRepository.save(expense(demoUser, wallet, fun, "Cinema", "6200", today.minusDays(12), friends));
            transactionRepository.save(expense(demoUser, bank, groceries, "Supermarket", "8450", today.minusDays(2)));

            // relleno para llegar a 25–40 con variedad
            Category[] cats = new Category[]{groceries, transport, coffee, fun, bills, health};
            Account[] accs = new Account[]{wallet, bank, cash};
            String[] descs = new String[]{
                    "Groceries", "Taxi", "Lunch", "Coffee", "Snacks",
                    "Streaming", "Gym", "Dinner", "Fuel", "Supplies"
            };

            Random r = new Random(42); // fijo = demo consistente
            for (int i = 0; i < 28; i++) { // +28 => total ~35
                Category c = cats[r.nextInt(cats.length)];
                Account a = accs[r.nextInt(accs.length)];
                String desc = descs[r.nextInt(descs.length)];
                int daysAgo = 1 + r.nextInt(26);
                BigDecimal amt = new BigDecimal(800 + r.nextInt(25000));

                transactionRepository.save(
                        expense(
                                demoUser,
                                a,
                                c,
                                desc,
                                amt.toPlainString(),
                                today.minusDays(daysAgo),
                                (i % 5 == 0) ? friends : null
                        )
                );
            }
        };
    }

    private static void ensureCategory(
            CategoryRepository categoryRepository,
            User owner,
            String name,
            String description,
            String colorHex
    ) {
        if (!categoryRepository.existsByOwnerAndNameIgnoreCase(owner, name)) {
            categoryRepository.save(Category.builder()
                    .owner(owner)
                    .name(name)
                    .description(description)
                    .colorHex(colorHex)
                    .active(true)
                    .build());
        }
    }

    private static Transaction expense(
            User owner,
            Account source,
            Category category,
            String desc,
            String amount,
            LocalDate date,
            Tag... tags
    ) {
        Transaction t = Transaction.builder()
                .owner(owner)
                .type(TransactionType.EXPENSE)
                .state(TransactionState.CONFIRMED) // clave para summary/donut
                .amount(new BigDecimal(amount))
                .operationDate(date)
                .description(desc)
                .sourceAccount(source)
                .category(category)
                .build();

        if (tags != null) for (Tag tag : tags) if (tag != null) t.getTags().add(tag);
        return t;
    }

    private static Transaction income(
            User owner,
            Account destination,
            Category category,
            String desc,
            String amount,
            LocalDate date,
            Tag... tags
    ) {
        Transaction t = Transaction.builder()
                .owner(owner)
                .type(TransactionType.INCOME)
                .state(TransactionState.CONFIRMED)
                .amount(new BigDecimal(amount))
                .operationDate(date)
                .description(desc)
                .destinationAccount(destination)
                .category(category)
                .build();

        if (tags != null) for (Tag tag : tags) if (tag != null) t.getTags().add(tag);
        return t;
    }
}
