package com.safebank.transaction.domain.repository;

import com.safebank.transaction.domain.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    // Cambiamos List por Page, y añadimos Pageable como último parámetro
    Page<Transaction> findBySourceAccountIdOrTargetIbanOrderByTransactionDateDesc(Long sourceAccountId, String targetIban, Pageable pageable);
}