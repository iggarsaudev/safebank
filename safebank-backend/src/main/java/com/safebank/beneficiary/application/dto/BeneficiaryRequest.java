package com.safebank.beneficiary.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BeneficiaryRequest(
        @NotBlank(message = "El nombre del contacto es obligatorio")
        @Size(max = 100, message = "El nombre es demasiado largo")
        String name,

        @NotBlank(message = "El IBAN es obligatorio")
        @Size(min = 15, max = 34, message = "El formato del IBAN no es válido")
        String iban
) {}