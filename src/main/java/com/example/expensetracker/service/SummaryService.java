package com.example.expensetracker.service;

import com.example.expensetracker.dto.summary.CategoryTotalResponse;
import com.example.expensetracker.dto.summary.SummaryResponse;
import com.example.expensetracker.enums.TransactionState;
import com.example.expensetracker.enums.TransactionType;
import com.example.expensetracker.repository.TransactionRepository;
import com.example.expensetracker.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class SummaryService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public SummaryService(UserRepository userRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    public SummaryResponse getSummary(Long ownerId, LocalDate from, LocalDate to, Integer top) {

        if (ownerId == null) throw new IllegalArgumentException("ownerId is required");
        if (from == null || to == null) throw new IllegalArgumentException("from and to are required");
        if (from.isAfter(to)) throw new IllegalArgumentException("from must be <= to");

        // valida que el user exista
        userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + ownerId));

        BigDecimal income = transactionRepository.sumAmountByOwnerAndTypeAndStateInPeriod(
                ownerId, TransactionType.INCOME, TransactionState.CONFIRMED, from, to
        );

        BigDecimal expense = transactionRepository.sumAmountByOwnerAndTypeAndStateInPeriod(
                ownerId, TransactionType.EXPENSE, TransactionState.CONFIRMED, from, to
        );

        BigDecimal net = income.subtract(expense);

        int topN = (top == null || top <= 0) ? 5 : Math.min(top, 50);

        List<CategoryTotalResponse> topCategories = transactionRepository.topCategoriesByOwnerInPeriod(
                ownerId,
                TransactionType.EXPENSE,
                TransactionState.CONFIRMED,
                from,
                to,
                PageRequest.of(0, topN)
        ).stream().map(row ->
                new CategoryTotalResponse(row.getCategoryId(), row.getCategoryName(), row.getTotal())
        ).toList();

        return new SummaryResponse(ownerId, from, to, income, expense, net, topCategories);
    }
}
