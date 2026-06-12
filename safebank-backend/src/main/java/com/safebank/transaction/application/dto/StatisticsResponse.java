package com.safebank.transaction.application.dto;

import java.math.BigDecimal;

public record StatisticsResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpense
) {}