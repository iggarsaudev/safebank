package com.safebank.transaction.infrastructure.scheduler;

import com.safebank.transaction.application.TransactionService;
import com.safebank.transaction.domain.ScheduledTransfer;
import com.safebank.transaction.domain.repository.ScheduledTransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TransferScheduler {

    private final ScheduledTransferRepository scheduledTransferRepository;
    private final TransactionService transactionService;

    // @Scheduled(fixedRate = 10000) significa que se ejecuta de forma asíncrona cada 10.000 milisegundos (10 segundos)
    @Scheduled(cron = "0 0 0 * * ?") // Se ejecuta todos los días a las 00:00:00 h
    public void processScheduledTransfers() {
        LocalDate today = LocalDate.now();
        
        // Buscamos todas las transferencias programadas que tengan fecha de hoy o pasada
        List<ScheduledTransfer> pendingTransfers = scheduledTransferRepository
                .findByActiveTrueAndNextExecutionDateLessThanEqual(today);

        if (!pendingTransfers.isEmpty()) {
            System.out.println("--- MOTOR CRON --- Detectadas " + pendingTransfers.size() + " transferencias programadas pendientes.");
            
            // Procesamos cada una de ellas de forma segura
            for (ScheduledTransfer st : pendingTransfers) {
                try {
                    transactionService.executeScheduledTransfer(st);
                } catch (Exception e) {
                    System.err.println("Fallo crítico en el Cron Job para la transferencia #" + st.getId() + ": " + e.getMessage());
                }
            }
        }
    }
}