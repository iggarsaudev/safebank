package com.safebank.transaction.application.dto;

import com.safebank.transaction.domain.TransferFrequency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TransactionRequest(
        @NotBlank(message = "El IBAN de destino es obligatorio")
        @Size(min = 15, max = 34, message = "El formato del IBAN no es válido")
        String targetIban,

        @NotNull(message = "El importe es obligatorio")
        @DecimalMin(value = "0.01", message = "El importe mínimo es 0.01")
        BigDecimal amount,

        @Size(max = 100, message = "El concepto no puede exceder los 100 caracteres")
        String concept,

        // Si viene nulo desde Angular, asumimos que es IMMEDIATE
        TransferFrequency frequency
) {}