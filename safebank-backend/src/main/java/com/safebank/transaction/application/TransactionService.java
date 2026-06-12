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
import com.safebank.transaction.domain.ScheduledTransfer;
import com.safebank.transaction.domain.TransferFrequency;
import com.safebank.transaction.domain.repository.ScheduledTransferRepository;
import java.time.LocalDate;
import java.math.BigDecimal;
import com.safebank.transaction.application.dto.StatisticsResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final ScheduledTransferRepository scheduledTransferRepository;
    private final com.safebank.notification.application.EmailService emailService;
    private final com.safebank.auth.domain.repository.UserRepository userRepository;
    
    // Añade el guardia de seguridad
    private final com.safebank.auth.application.OtpService otpService;

    /**
     * Realiza una transferencia inmediata o programa una transferencia periódica.
     */
    @Transactional
    public void makeTransfer(Long sourceUserId, TransactionRequest request) {
        Account sourceAccount = accountRepository.findByUserId(sourceUserId)
                .orElseThrow(() -> new RuntimeException("Cuenta origen no encontrada"));

        if (sourceAccount.getIban().equals(request.targetIban())) {
            throw new RuntimeException("No puedes enviarte dinero a tu propia cuenta");
        }

        // Seguridad OTP
        BigDecimal otpThreshold = new BigDecimal("1000.00");

        // Si el importe es mayor o igual a 1.000€...
        if (request.amount().compareTo(otpThreshold) >= 0) {
            // 1. Comprobamos si nos han mandado el código
            if (request.otpCode() == null || request.otpCode().isBlank()) {
                throw new RuntimeException("Se requiere código de seguridad OTP para operaciones superiores a 1.000€");
            }
            
            // 2. Comprobamos si NO es la llave maestra de nuestro portfolio
            if (!request.otpCode().equals("123456")) {
                // Si es un usuario normal, validamos contra la base de datos
                if (!otpService.isOtpValid(sourceUserId, request.otpCode())) {
                    throw new RuntimeException("El código de seguridad es incorrecto o ha caducado");
                }
                // Si es correcto, lo quemamos para que un hacker no pueda reutilizarlo
                otpService.markOtpAsUsed(sourceUserId);
            }
            // Si es "123456", se salta el if de arriba y permite la operación de inmediato
        }

        // Si la frecuencia es MONTHLY, guardamos la orden en vez de mover el dinero ya
        if (request.frequency() == TransferFrequency.MONTHLY) {
            ScheduledTransfer scheduledTransfer = ScheduledTransfer.builder()
                    .sourceUserId(sourceUserId)
                    .targetIban(request.targetIban())
                    .amount(request.amount())
                    .concept(request.concept() != null && !request.concept().isBlank() ? request.concept() : "Transferencia Mensual Programada")
                    .frequency(TransferFrequency.MONTHLY)
                    .nextExecutionDate(LocalDate.now()) // Se ejecuta por primera vez ¡hoy mismo!
                    .active(true)
                    .build();

            scheduledTransferRepository.save(scheduledTransfer);
            return; // Fin del flujo para las programadas
        }

        // FLUJO INMEDIATO ESTÁNDAR
        if (sourceAccount.getBalance().compareTo(request.amount()) < 0) {
            throw new RuntimeException("Saldo insuficiente para realizar la transferencia");
        }

        Account targetAccount = accountRepository.findByIban(request.targetIban())
                .orElseThrow(() -> new RuntimeException("El IBAN de destino no existe en nuestro sistema"));

        sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.amount()));
        targetAccount.setBalance(targetAccount.getBalance().add(request.amount()));

        accountRepository.save(sourceAccount);
        accountRepository.save(targetAccount);

        Transaction transaction = Transaction.builder()
                .sourceAccountId(sourceAccount.getId())
                .targetIban(request.targetIban())
                .amount(request.amount())
                .concept(request.concept() != null && !request.concept().isBlank() ? request.concept() : "Transferencia estándar")
                .build();

        transactionRepository.save(transaction);

        // Notificación por email al receptor
        userRepository.findById(targetAccount.getUserId()).ifPresent(targetUser -> {
            emailService.sendTransferReceivedEmail(targetUser.getEmail(), request.amount(), transaction.getConcept());
        });

        // Notificación por email al emisor
        userRepository.findById(sourceUserId).ifPresent(sourceUser -> {
            emailService.sendTransferSentEmail(sourceUser.getEmail(), request.amount(), request.targetIban(), transaction.getConcept());
        });
    }

    /**
     * LÓGICA DEL MOTOR AUTOMÁTICO: Ejecuta una transferencia programada individual
     */
    @Transactional
    public void executeScheduledTransfer(ScheduledTransfer st) {
        Account sourceAccount = accountRepository.findByUserId(st.getSourceUserId()).orElse(null);
        Account targetAccount = accountRepository.findByIban(st.getTargetIban()).orElse(null);

        // Si alguna cuenta no existe o no hay saldo, cancelamos esta ejecución pero mantenemos la orden activa para reintentar
        if (sourceAccount == null || targetAccount == null || sourceAccount.getBalance().compareTo(st.getAmount()) < 0) {
            System.err.println("Error procesando pago automático #" + st.getId() + ": saldo insuficiente o cuenta inválida.");
            return;
        }

        // Ejecutamos el movimiento financiero
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(st.getAmount()));
        targetAccount.setBalance(targetAccount.getBalance().add(st.getAmount()));
        accountRepository.save(sourceAccount);
        accountRepository.save(targetAccount);

        // Registramos el movimiento inmutable en el historial real
        Transaction transaction = Transaction.builder()
                .sourceAccountId(sourceAccount.getId())
                .targetIban(st.getTargetIban())
                .amount(st.getAmount())
                .concept("[Automático] " + st.getConcept())
                .build();
        transactionRepository.save(transaction);

        // Actualizamos la orden programada para el MES que viene (+1 mes)
        st.setNextExecutionDate(st.getNextExecutionDate().plusMonths(1));
        scheduledTransferRepository.save(st);
        System.out.println("Pago recurrente #" + st.getId() + " ejecutado con éxito. Próxima fecha: " + st.getNextExecutionDate());

        // Notificación por email al receptor
        userRepository.findById(targetAccount.getUserId()).ifPresent(targetUser -> {
            emailService.sendTransferReceivedEmail(targetUser.getEmail(), st.getAmount(), "[Automático] " + st.getConcept());
        });

        // Notificación por email al emisor
        userRepository.findById(st.getSourceUserId()).ifPresent(sourceUser -> {
            emailService.sendScheduledPaymentExecutedEmail(sourceUser.getEmail(), st.getAmount(), st.getConcept());
        });
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

    /**
     * Devuelve las transferencias programadas activas de un usuario.
     */
    @Transactional(readOnly = true)
    public List<ScheduledTransfer> getMyScheduledTransfers(Long userId) {
        return scheduledTransferRepository.findBySourceUserIdAndActiveTrue(userId);
    }

    /**
     * Cancela y elimina una transferencia programada asegurando que pertenezca al usuario.
     */
    @Transactional
    public void cancelScheduledTransfer(Long userId, Long scheduledTransferId) {
        ScheduledTransfer st = scheduledTransferRepository.findById(scheduledTransferId)
                .orElseThrow(() -> new RuntimeException("Pago programado no encontrado"));

        // ¡SEGURIDAD! Verificamos que la orden pertenezca al usuario que la quiere borrar
        if (!st.getSourceUserId().equals(userId)) {
            throw new RuntimeException("No tienes permiso para cancelar esta operación");
        }

        // Lo eliminamos físicamente de la base de datos
        scheduledTransferRepository.delete(st);
    }

    /**
     * Devuelve el sumatorio total de ingresos y gastos del usuario para las gráficas.
     */
    @Transactional(readOnly = true)
    public StatisticsResponse getMyStatistics(Long userId) {
        Account myAccount = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

        java.math.BigDecimal income = transactionRepository.sumTotalIncome(myAccount.getIban());
        java.math.BigDecimal expense = transactionRepository.sumTotalExpense(myAccount.getId());

        return new StatisticsResponse(income, expense);
    }
}