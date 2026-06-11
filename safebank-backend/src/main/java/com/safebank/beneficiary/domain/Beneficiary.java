package com.safebank.beneficiary.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "beneficiaries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Beneficiary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación lógica: a qué usuario le pertenece este contacto
    @Column(nullable = false)
    private Long userId;

    // El alias que le pongas, ej. "Pepe Alquiler"
    @Column(nullable = false, length = 100)
    private String name;

    // El IBAN de esta persona
    @Column(nullable = false, length = 34)
    private String iban;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}