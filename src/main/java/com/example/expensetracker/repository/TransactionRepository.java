package com.example.expensetracker.repository;

import com.example.expensetracker.enums.TransactionState;
import com.example.expensetracker.enums.TransactionType;
import com.example.expensetracker.model.Transaction;
import com.example.expensetracker.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /* =====================================================
       OWNERSHIP / LISTADO
       ===================================================== */

    // Listado de transacciones del usuario
    List<Transaction> findAllByOwner(User owner);

    // Por período (del usuario)
    List<Transaction> findAllByOwnerAndOperationDateBetween(User owner, LocalDate from, LocalDate to);

    // Para update/cancel/confirm: transacción solo si es del usuario
    Optional<Transaction> findByIdAndOwnerId(Long id, Long ownerId);

    // Cuenta como Source o Destination (del usuario)
    List<Transaction> findByOwnerAndSourceAccount_IdOrOwnerAndDestinationAccount_Id(
            User owner1, Long sourceAccountId,
            User owner2, Long destinationAccountId,
            Pageable pageable
    );

    // Cuenta como Source o Destination dentro de un período (del usuario)
    List<Transaction> findByOwnerAndOperationDateBetweenAndSourceAccount_IdOrOwnerAndOperationDateBetweenAndDestinationAccount_Id(
            User owner1, LocalDate from1, LocalDate to1, Long sourceAccountId,
            User owner2, LocalDate from2, LocalDate to2, Long destinationAccountId,
            Pageable pageable
    );

    /* =====================================================
       AGREGACIÓN (SALDOS) - SOLO CONFIRMED (sin ownership extra porque ya filtras por accountId)
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

    /* =====================================================
       SUMMARY (por ownerId)
       ===================================================== */

    @Query("""
    select coalesce(sum(t.amount), 0)
    from Transaction t
    where t.owner.id = :ownerId
      and t.state = :state
      and t.type = :type
      and t.operationDate between :from and :to
    """)
    BigDecimal sumAmountByOwnerAndTypeAndStateInPeriod(
            @Param("ownerId") Long ownerId,
            @Param("type") TransactionType type,
            @Param("state") TransactionState state,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    interface CategoryTotalRow {
        Long getCategoryId();
        String getCategoryName();
        BigDecimal getTotal();
    }

    @Query("""
    select
      c.id as categoryId,
      c.name as categoryName,
      coalesce(sum(t.amount), 0) as total
    from Transaction t
    join t.category c
    where t.owner.id = :ownerId
      and t.state = :state
      and t.type = :type
      and t.operationDate between :from and :to
    group by c.id, c.name
    order by sum(t.amount) desc
    """)
    List<CategoryTotalRow> topCategoriesByOwnerInPeriod(
            @Param("ownerId") Long ownerId,
            @Param("type") TransactionType type,
            @Param("state") TransactionState state,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            Pageable pageable
    );
}
