package com.example.moneytracker.wallet;

import com.example.moneytracker.wallet.dto.BalanceResponse;
import com.example.moneytracker.wallet.dto.CreateWalletRequest;
import com.example.moneytracker.wallet.dto.WalletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(@Valid @RequestBody CreateWalletRequest request) {
        WalletResponse response = walletService.createWallet(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<WalletResponse> getWallets() {
        return walletService.listWallets();
    }

    @GetMapping("/balance")
    public BalanceResponse getBalance() {
        return walletService.getBalance();
    }
}

