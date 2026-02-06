package com.example.moneytracker.wallet.dto;

import java.math.BigDecimal;

public record BalanceResponse(
        BigDecimal cash,
        BigDecimal online,
        BigDecimal total
) {
}

