package com.safebank.transaction.domain.repository;

import com.safebank.transaction.domain.ScheduledTransfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ScheduledTransferRepository extends JpaRepository<ScheduledTransfer, Long> {

    // El motor buscará las transferencias activas cuya fecha de ejecución sea HOY o ANTES
    List<ScheduledTransfer> findByActiveTrueAndNextExecutionDateLessThanEqual(LocalDate date);

    // Añade esto para recuperar los pagos programados de un usuario específico
    java.util.List<ScheduledTransfer> findBySourceUserIdAndActiveTrue(Long sourceUserId);
}