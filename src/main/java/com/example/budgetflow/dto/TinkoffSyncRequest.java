package com.example.budgetflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TinkoffSyncRequest(
        @NotBlank(message = "Токен обязателен")
        @Size(min = 20, message = "Токен должен быть не короче 20 символов")
        String token,

        String accountId,

        @NotNull(message = "Режим синхронизации обязателен")
        TinkoffSyncMode mode
) {
}



