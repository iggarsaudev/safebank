package com.safebank.transaction.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "scheduled_transfers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long sourceUserId;

    @Column(nullable = false, length = 34)
    private String targetIban;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(length = 100)
    private String concept;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferFrequency frequency;

    // Qué día le toca ejecutarse la próxima vez
    @Column(nullable = false)
    private LocalDate nextExecutionDate;

    // Por si el usuario quiere pausar el pago automático sin borrarlo
    @Column(nullable = false)
    private boolean active;
}