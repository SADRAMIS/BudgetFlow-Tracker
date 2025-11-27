package com.example.budgetflow.service;

import com.example.budgetflow.dto.TinkoffSyncMode;
import com.example.budgetflow.dto.TinkoffSyncRequest;
import com.example.budgetflow.dto.TinkoffSyncResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class TinkoffIntegrationService {

    public TinkoffSyncResponse enqueueSync(TinkoffSyncRequest request) {
        log.info("Получен запрос синхронизации Т-Инвестиций: mode={}, accountId={}",
                request.mode(), request.accountId());
        CompletableFuture.runAsync(() -> performSync(request));

        Map<String, Object> summary = new HashMap<>();
        summary.put("accountId", request.accountId());
        summary.put("mode", request.mode().name());
        summary.put("estimatedOps", estimateOperations(request.mode()));

        return new TinkoffSyncResponse(
                "QUEUED",
                "Синхронизация поставлена в очередь",
                request.mode(),
                Instant.now(),
                summary
        );
    }

    private void performSync(TinkoffSyncRequest request) {
        try {
            // Здесь должна быть реальная работа с InvestApi или импортом отчётов.
            Thread.sleep(1500L);
            switch (request.mode()) {
                case HISTORY -> log.info("Импортируем историю сделок для {}", request.accountId());
                case POSITIONS -> log.info("Сверяем позиции для {}", request.accountId());
                case DIVIDENDS -> log.info("Подтягиваем дивиденды для {}", request.accountId());
            }
            log.info("Синхронизация Т-Инвестиций завершена: mode={}, accountId={}",
                    request.mode(), request.accountId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Синхронизация прервана: {}", e.getMessage());
        } catch (Exception ex) {
            log.error("Ошибка интеграции с Т-Инвестициями: {}", ex.getMessage(), ex);
        }
    }

    private int estimateOperations(TinkoffSyncMode mode) {
        return switch (mode) {
            case HISTORY -> 250;
            case POSITIONS -> 25;
            case DIVIDENDS -> 40;
        };
    }
}

