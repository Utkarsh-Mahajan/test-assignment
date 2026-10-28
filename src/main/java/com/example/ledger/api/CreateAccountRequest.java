package com.example.ledger.api;

import jakarta.validation.constraints.NotBlank;

public record CreateAccountRequest(@NotBlank(message = "Account name is required") String name) {
}
