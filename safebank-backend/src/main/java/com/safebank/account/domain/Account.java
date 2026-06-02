package com.safebank.account.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts", uniqueConstraints = {
    @UniqueConstraint(columnNames = "iban")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // el iban será único para cada cuenta
    @Column(nullable = false, unique = true, length = 34)
    private String iban;

    // usamos bigdecimal para evitar pérdida de céntimos en operaciones bancarias
    @Column(nullable = false)
    private BigDecimal balance;

    // guardamos solo el id del usuario para mantener los dominios desacoplados
    @Column(nullable = false)
    private Long userId;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        // si no se especifica saldo inicial, arranca en 0
        if (this.balance == null) {
            this.balance = BigDecimal.ZERO;
        }
    }
}