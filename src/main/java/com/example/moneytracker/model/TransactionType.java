package com.example.moneytracker.model;

public enum TransactionType {
    RECEIVED,   // someone gave you money
    GIVEN,      // you gave someone money
    EXPENSE,    // spent money, no person
    INCOME,     // income, no person
    TRANSFER    // wallet-to-wallet internal transfer
}

