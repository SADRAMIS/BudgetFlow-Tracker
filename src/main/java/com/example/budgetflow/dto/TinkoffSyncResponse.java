package com.example.budgetflow.dto;

import java.time.Instant;
import java.util.Map;

public record TinkoffSyncResponse(
        String status,
        String message,
        TinkoffSyncMode mode,
        Instant queuedAt,
        Map<String, Object> summary
) {
}

