package com.example.moneytracker.person.dto;

import java.math.BigDecimal;

public record PersonSummaryResponse(
        Long id,
        String name,
        String phone,
        String notes,
        BigDecimal totalReceived,
        BigDecimal totalGiven,
        BigDecimal netBalance,
        String status
) {
}

