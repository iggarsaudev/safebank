package com.safebank.transaction.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionHistoryResponse(
        Long id,
        String concept,
        BigDecimal amount,
        LocalDateTime transactionDate,
        boolean isIncoming, // ¿Es dinero entrante?
        String otherIban    // Si envío, es el destino. Si recibo, es el origen.
) {}