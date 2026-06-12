package com.safebank.transaction.domain.repository;

import com.safebank.transaction.domain.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    // Cambiamos List por Page, y añadimos Pageable como último parámetro
    Page<Transaction> findBySourceAccountIdOrTargetIbanOrderByTransactionDateDesc(Long sourceAccountId, String targetIban, Pageable pageable);

    // Estadísticas
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.targetIban = :iban")
    java.math.BigDecimal sumTotalIncome(String iban);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.sourceAccountId = :accountId")
    java.math.BigDecimal sumTotalExpense(Long accountId);
}