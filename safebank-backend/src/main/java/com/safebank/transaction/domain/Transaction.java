package com.safebank.transaction.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // guardamos solo el id de la cuenta origen para no acoplar los dominios
    @Column(nullable = false)
    private Long sourceAccountId;

    // el iban al que enviamos el dinero
    @Column(nullable = false, length = 34)
    private String targetIban;

    // cantidad de dinero transferida (siempre usamos bigdecimal para evitar fallos de precisión)
    @Column(nullable = false)
    private BigDecimal amount;

    // concepto o motivo de la transferencia
    @Column(length = 100)
    private String concept;

    @Column(nullable = false)
    private LocalDateTime transactionDate;

    // se ejecuta automáticamente justo antes de guardar en la base de datos
    @PrePersist
    protected void onCreate() {
        this.transactionDate = LocalDateTime.now();
    }
}