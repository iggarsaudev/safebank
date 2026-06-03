package com.safebank.transaction.domain.repository;

import com.safebank.transaction.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    // Buscamos donde seas el origen (envías) O donde seas el destino (recibes)
    List<Transaction> findBySourceAccountIdOrTargetIbanOrderByTransactionDateDesc(Long sourceAccountId, String targetIban);
}