package com.example.moneytracker.auth.dto;

public record AuthResponse(
        Long userId,
        String email,
        String token
) {
}

