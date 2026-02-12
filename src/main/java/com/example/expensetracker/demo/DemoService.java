package com.example.expensetracker.demo;

import com.example.expensetracker.enums.AccountType;
import com.example.expensetracker.enums.TransactionState;
import com.example.expensetracker.enums.TransactionType;
import com.example.expensetracker.model.*;
import com.example.expensetracker.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class DemoService {

    private static final String DEMO_EMAIL = "demo@example.com";
    private static final String DEMO_PASS = "demo12345";

    private final UserRepository userRepository;
    private final CurrencyRepository currencyRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void resetDemo() {
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

        User demoUser = userRepository.findByEmail(DEMO_EMAIL)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .name("Demo User")
                                .email(DEMO_EMAIL)
                                .passwordHash(passwordEncoder.encode(DEMO_PASS))
                                .defaultCurrency(ars)
                                .createdAt(LocalDateTime.now())
                                .active(true)
                                .build()
                ));

        // --------------------
        // RESET (orden por FK)
        // --------------------
        transactionRepository.deleteByOwner(demoUser);
        accountRepository.deleteByOwner(demoUser);
        categoryRepository.deleteByOwner(demoUser);
        tagRepository.deleteByOwner(demoUser);

        // --------------------
        // SEED
        // --------------------
        Account cash = accountRepository.save(acc(demoUser, "Cash", AccountType.CASH, ars, "35000"));
        Account bank = accountRepository.save(acc(demoUser, "Bank", AccountType.BANK, ars, "450000"));
        Account wallet = accountRepository.save(acc(demoUser, "Wallet", AccountType.DIGITAL_WALLET, ars, "150000"));

        Category groceries = categoryRepository.save(cat(demoUser, "Groceries", "Supermarkets & food", "#22c55e"));
        Category rent      = categoryRepository.save(cat(demoUser, "Rent", "Monthly rent", "#ef4444"));
        Category transport = categoryRepository.save(cat(demoUser, "Transport", "Bus, taxi, fuel", "#3b82f6"));
        Category coffee    = categoryRepository.save(cat(demoUser, "Coffee", "Coffee & snacks", "#a855f7"));
        Category health    = categoryRepository.save(cat(demoUser, "Health", "Pharmacy & medical", "#f97316"));
        Category fun       = categoryRepository.save(cat(demoUser, "Fun", "Movies, outings", "#14b8a6"));
        Category bills     = categoryRepository.save(cat(demoUser, "Bills", "Services & subscriptions", "#64748b"));
        Category salary    = categoryRepository.save(cat(demoUser, "Salary", "Income", "#0ea5e9"));

        Tag utn = tagRepository.save(tag(demoUser, "UTN"));
        Tag friends = tagRepository.save(tag(demoUser, "Friends"));

        LocalDate today = LocalDate.now();

        // incomes
        transactionRepository.save(income(demoUser, bank, salary, "Salary", "650000", today.minusDays(3), utn));
        transactionRepository.save(income(demoUser, wallet, salary, "Freelance", "90000", today.minusDays(18)));

        // base expenses
        transactionRepository.save(expense(demoUser, bank, rent, "Rent", "120000", today.minusDays(10)));
        transactionRepository.save(expense(demoUser, bank, bills, "Internet", "18000", today.minusDays(7)));
        transactionRepository.save(expense(demoUser, wallet, coffee, "Coffee", "1800", today.minusDays(1)));
        transactionRepository.save(expense(demoUser, cash, transport, "Bus card", "4500", today.minusDays(5)));
        transactionRepository.save(expense(demoUser, bank, health, "Pharmacy", "9300", today.minusDays(20)));
        transactionRepository.save(expense(demoUser, wallet, fun, "Cinema", "6200", today.minusDays(12), friends));
        transactionRepository.save(expense(demoUser, bank, groceries, "Supermarket", "8450", today.minusDays(2)));

        // filler
        Category[] cats = new Category[]{groceries, transport, coffee, fun, bills, health};
        Account[] accs = new Account[]{wallet, bank, cash};
        String[] descs = {"Groceries","Taxi","Lunch","Coffee","Snacks","Streaming","Gym","Dinner","Fuel","Supplies"};

        Random r = new Random(42);
        for (int i = 0; i < 28; i++) {
            Category c = cats[r.nextInt(cats.length)];
            Account a = accs[r.nextInt(accs.length)];
            String desc = descs[r.nextInt(descs.length)];
            int daysAgo = 1 + r.nextInt(26);
            BigDecimal amt = new BigDecimal(800 + r.nextInt(25000));

            transactionRepository.save(expense(
                    demoUser, a, c, desc, amt.toPlainString(), today.minusDays(daysAgo),
                    (i % 5 == 0) ? friends : null
            ));
        }
    }

    private static Account acc(User owner, String name, AccountType type, Currency currency, String initial) {
        return Account.builder()
                .owner(owner)
                .name(name)
                .type(type)
                .currency(currency)
                .initialBalance(new BigDecimal(initial))
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private static Category cat(User owner, String name, String desc, String color) {
        return Category.builder()
                .owner(owner)
                .name(name)
                .description(desc)
                .colorHex(color)
                .active(true)
                .build();
    }

    private static Tag tag(User owner, String name) {
        return Tag.builder().owner(owner).name(name).active(true).build();
    }

    private static Transaction expense(User owner, Account source, Category category, String desc, String amount, LocalDate date, Tag... tags) {
        Transaction t = Transaction.builder()
                .owner(owner)
                .type(TransactionType.EXPENSE)
                .state(TransactionState.CONFIRMED)
                .amount(new BigDecimal(amount))
                .operationDate(date)
                .description(desc)
                .sourceAccount(source)
                .category(category)
                .build();
        if (tags != null) for (Tag tag : tags) if (tag != null) t.getTags().add(tag);
        return t;
    }

    private static Transaction income(User owner, Account destination, Category category, String desc, String amount, LocalDate date, Tag... tags) {
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
