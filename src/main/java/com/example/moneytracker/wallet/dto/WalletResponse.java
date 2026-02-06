package com.example.moneytracker.wallet.dto;

import com.example.moneytracker.model.WalletType;

import java.math.BigDecimal;

public record WalletResponse(
        Long id,
        WalletType type,
        BigDecimal balance
) {
}

