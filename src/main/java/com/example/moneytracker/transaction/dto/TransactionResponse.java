package com.example.moneytracker.transaction.dto;

import com.example.moneytracker.model.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        Long id,
        Long personId,
        Long fromWalletId,
        Long toWalletId,
        BigDecimal amount,
        TransactionType transactionType,
        String description,
        Instant date,
        Instant createdAt
) {
}

