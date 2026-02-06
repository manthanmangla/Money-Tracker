package com.example.moneytracker.transaction.dto;

import com.example.moneytracker.model.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record CreateTransactionRequest(

        Long personId,

        Long fromWalletId,

        Long toWalletId,

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal amount,

        @NotNull
        TransactionType transactionType,

        String description,

        Instant date
) {
}

