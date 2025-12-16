package com.example.expensetracker.repository;

import com.example.expensetracker.enums.TransactionState;
import com.example.expensetracker.enums.TransactionType;
import com.example.expensetracker.model.Transaction;
import com.example.expensetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /* =====================================================
       CONSULTAS DE LISTADO / HISTORIAL
       ===================================================== */

    List<Transaction> findByOwner(User owner);

    List<Transaction> findByOwnerAndOperationDateBetween(
            User owner,
            LocalDate from,
            LocalDate to
    );

    /* =====================================================
       CONSULTAS DE AGREGACIÓN (CÁLCULO DE SALDO)
       SOLO TRANSACCIONES CONFIRMED
       ===================================================== */

    // EXPENSE o TRANSFER que salen de una cuenta
    @Query("""
           select coalesce(sum(t.amount), 0)
           from Transaction t
           where t.type = :type
             and t.state = :state
             and t.sourceAccount.id = :accountId
           """)
    BigDecimal sumByTypeAndStateAndSourceAccount(
            @Param("type") TransactionType type,
            @Param("state") TransactionState state,
            @Param("accountId") Long accountId
    );

    // INCOME o TRANSFER que entran a una cuenta
    @Query("""
           select coalesce(sum(t.amount), 0)
           from Transaction t
           where t.type = :type
             and t.state = :state
             and t.destinationAccount.id = :accountId
           """)
    BigDecimal sumByTypeAndStateAndDestinationAccount(
            @Param("type") TransactionType type,
            @Param("state") TransactionState state,
            @Param("accountId") Long accountId
    );
}
