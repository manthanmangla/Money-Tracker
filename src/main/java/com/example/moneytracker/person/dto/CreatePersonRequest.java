package com.example.moneytracker.person.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePersonRequest(

        @NotBlank
        String name,

        String phone,

        String notes
) {
}

