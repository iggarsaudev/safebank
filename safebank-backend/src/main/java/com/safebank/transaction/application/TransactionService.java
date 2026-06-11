package com.safebank.transaction.application;

import com.safebank.account.domain.Account;
import com.safebank.account.domain.repository.AccountRepository;
import com.safebank.transaction.application.dto.TransactionHistoryResponse;
import com.safebank.transaction.application.dto.TransactionRequest;
import com.safebank.transaction.domain.Transaction;
import com.safebank.transaction.domain.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    /**
     * Realiza una transferencia bancaria inmutable y segura entre dos cuentas del sistema.
     * La anotación @Transactional garantiza la atomicidad (propiedades ACID): o se completa todo o se hace rollback.
     */
    @Transactional
    public void makeTransfer(Long sourceUserId, TransactionRequest request) {
        // 1. Localizamos la cuenta del usuario que está intentando hacer la transferencia
        Account sourceAccount = accountRepository.findByUserId(sourceUserId)
                .orElseThrow(() -> new RuntimeException("Cuenta origen no encontrada"));

        // 2. Validación de seguridad: no puedes enviarte dinero a ti mismo
        if (sourceAccount.getIban().equals(request.targetIban())) {
            throw new RuntimeException("No puedes enviarte dinero a tu propia cuenta");
        }

        // 3. Validación financiera: comprobamos que tenga saldo suficiente
        if (sourceAccount.getBalance().compareTo(request.amount()) < 0) {
            throw new RuntimeException("Saldo insuficiente para realizar la transferencia");
        }

        // 4. Localizamos la cuenta destino (si no existe, la transacción se aborta por completo)
        Account targetAccount = accountRepository.findByIban(request.targetIban())
                .orElseThrow(() -> new RuntimeException("El IBAN de destino no existe en nuestro sistema"));

        // 5. Realizamos la operación matemática (restamos al origen y sumamos al destino)
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.amount()));
        targetAccount.setBalance(targetAccount.getBalance().add(request.amount()));

        // Persistimos los nuevos saldos actualizados en la base de datos
        accountRepository.save(sourceAccount);
        accountRepository.save(targetAccount);

        // 6. Generamos y guardamos el registro inmutable de la transferencia en el histórico
        Transaction transaction = Transaction.builder()
                .sourceAccountId(sourceAccount.getId())
                .targetIban(request.targetIban())
                .amount(request.amount())
                .concept(request.concept() != null && !request.concept().isBlank() ? request.concept() : "Transferencia estándar")
                .build();

        transactionRepository.save(transaction);
    }

    /**
     * Recupera el historial completo de movimientos financieros vinculados a un usuario,
     * reflejando tanto el dinero saliente (enviado) como el entrante (recibido).
     */
    @Transactional(readOnly = true)
    public Page<TransactionHistoryResponse> getMyTransactions(Long userId, int page, int size) {
        Account myAccount = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

        // Creamos el objeto de paginación (ej: página 0, tamaño 5)
        Pageable pageable = PageRequest.of(page, size);

        // Pedimos solo ese "trozo" a la base de datos
        Page<Transaction> transactionsPage = transactionRepository
                .findBySourceAccountIdOrTargetIbanOrderByTransactionDateDesc(myAccount.getId(), myAccount.getIban(), pageable);

        // El objeto Page tiene su propio método map que mantiene los metadatos (total de páginas, etc.)
        return transactionsPage.map(tx -> {
            boolean isIncoming = tx.getTargetIban().equals(myAccount.getIban());
            String otherIban;

            if (isIncoming) {
                otherIban = accountRepository.findById(tx.getSourceAccountId())
                        .map(Account::getIban)
                        .orElse("Cuenta Externa / Desconocida");
            } else {
                otherIban = tx.getTargetIban();
            }

            return new TransactionHistoryResponse(
                    tx.getId(),
                    tx.getConcept(),
                    tx.getAmount(),
                    tx.getTransactionDate(),
                    isIncoming,
                    otherIban
            );
        });
    }

    /**
     * Recupera una transacción específica para generar su justificante asegurando que pertenezca al usuario.
     */
    @Transactional(readOnly = true)
    public TransactionHistoryResponse getTransactionReceipt(Long userId, Long transactionId) {
        Account myAccount = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

        Transaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));

        // Verificamos que la cuenta del usuario sea la de origen o la de destino
        if (!tx.getSourceAccountId().equals(myAccount.getId()) && !tx.getTargetIban().equals(myAccount.getIban())) {
            throw new RuntimeException("No tienes permiso para ver esta transacción");
        }

        // Mapeamos al DTO
        boolean isIncoming = tx.getTargetIban().equals(myAccount.getIban());
        String otherIban = isIncoming
                ? accountRepository.findById(tx.getSourceAccountId()).map(Account::getIban).orElse("Cuenta Externa")
                : tx.getTargetIban();

        return new TransactionHistoryResponse(tx.getId(), tx.getConcept(), tx.getAmount(), tx.getTransactionDate(), isIncoming, otherIban);
    }
}