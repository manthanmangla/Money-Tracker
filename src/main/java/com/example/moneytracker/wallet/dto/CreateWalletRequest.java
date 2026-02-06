package com.example.moneytracker.wallet.dto;

import com.example.moneytracker.model.WalletType;
import jakarta.validation.constraints.NotNull;

public record CreateWalletRequest(

        @NotNull
        WalletType type
) {
}

