package com.safebank.transaction.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TransactionRequest(
        @NotBlank(message = "el iban de destino es obligatorio")
        String targetIban,

        @NotNull(message = "la cantidad es obligatoria")
        @Positive(message = "la cantidad debe ser mayor que cero")
        BigDecimal amount,

        @Size(max = 100, message = "el concepto no puede exceder los 100 caracteres")
        String concept
) {}