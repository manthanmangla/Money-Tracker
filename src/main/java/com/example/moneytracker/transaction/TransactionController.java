package com.example.moneytracker.transaction;

import com.example.moneytracker.model.TransactionType;
import com.example.moneytracker.model.WalletType;
import com.example.moneytracker.transaction.dto.CreateTransactionRequest;
import com.example.moneytracker.transaction.dto.TransactionResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody CreateTransactionRequest request) {
        TransactionResponse response = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/reverse")
    public ResponseEntity<TransactionResponse> reverseTransaction(@PathVariable("id") Long id) {
        TransactionResponse response = transactionService.reverseTransaction(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<TransactionResponse> listTransactions(
            @RequestParam(value = "wallet", required = false) WalletType wallet,
            @RequestParam(value = "type", required = false) TransactionType type,
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return transactionService.listTransactions(wallet, type, from, to);
    }
}

