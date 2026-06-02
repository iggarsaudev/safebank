package com.safebank.account.application.dto;

import java.math.BigDecimal;

public record AccountResponse(
        String iban,
        BigDecimal balance
) {}