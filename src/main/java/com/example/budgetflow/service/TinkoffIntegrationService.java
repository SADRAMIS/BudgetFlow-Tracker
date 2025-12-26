package com.example.budgetflow.service;

import com.example.budgetflow.dto.TinkoffSyncMode;
import com.example.budgetflow.dto.TinkoffSyncRequest;
import com.example.budgetflow.dto.TinkoffSyncResponse;
import com.example.budgetflow.entity.*;
import com.example.budgetflow.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class TinkoffIntegrationService {

    private final UserService userService;
    private final AccountService accountService;
    private final AssetService assetService;
    private final TradeRepository tradeRepository;
    private final AccrualRepository accrualRepository;
    private final ImportLogRepository importLogRepository;

    public TinkoffSyncResponse enqueueSync(TinkoffSyncRequest request, Long userId) {
        log.info("Получен запрос синхронизации Т-Инвестиций: userId={}, mode={}, accountId={}",
                userId, request.mode(), request.accountId());
        
        CompletableFuture.runAsync(() -> performSync(request, userId));

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

    @Transactional
    private void performSync(TinkoffSyncRequest request, Long userId) {
        ImportLog logEntry = new ImportLog();
        logEntry.setUser(userService.getUserById(userId));
        logEntry.setSource("TINKOFF_API");
        logEntry.setImportDate(java.time.LocalDateTime.now());
        logEntry.setStatus("IN_PROGRESS");
        
        try {
            String accountId = request.accountId() != null ? request.accountId() : "default";
            
            int opsCount = 0;
            int assetsCount = 0;

            // TODO: Заменить на реальный вызов Tinkoff Invest API
            // Для работы требуется:
            // 1. Получить токен из request.token()
            // 2. Инициализировать InvestApi
            // 3. Вызвать соответствующие методы API в зависимости от режима
            
            if (request.mode() == TinkoffSyncMode.HISTORY || request.mode() == TinkoffSyncMode.ALL) {
                opsCount += importOperationsMock(accountId, userId);
            }
            
            if (request.mode() == TinkoffSyncMode.POSITIONS || request.mode() == TinkoffSyncMode.ALL) {
                assetsCount += importPositionsMock(accountId, userId);
            }
            
            if (request.mode() == TinkoffSyncMode.DIVIDENDS || request.mode() == TinkoffSyncMode.ALL) {
                opsCount += importDividendsMock(accountId, userId);
            }

            logEntry.setStatus("SUCCESS");
            logEntry.setOperationsImported(opsCount);
            logEntry.setAssetsImported(assetsCount);
            logEntry.setDetails(String.format("{\"accountId\":\"%s\",\"operations\":%d,\"assets\":%d}", 
                    accountId, opsCount, assetsCount));
            
            log.info("Синхронизация Т-Инвестиций завершена: userId={}, operations={}, assets={}", 
                    userId, opsCount, assetsCount);
        } catch (Exception ex) {
            log.error("Ошибка интеграции с Т-Инвестициями: {}", ex.getMessage(), ex);
            logEntry.setStatus("FAILED");
            logEntry.setErrorMessage(ex.getMessage());
        } finally {
            importLogRepository.save(logEntry);
        }
    }

    private int importOperationsMock(String accountId, Long userId) {
        try {
            Account account = findOrCreateAccount(accountId, userId);
            // В реальной реализации здесь будет вызов API
            log.info("Импорт операций для аккаунта {}", accountId);
            return 0; // Заглушка
        } catch (Exception e) {
            log.error("Ошибка импорта операций: {}", e.getMessage(), e);
            return 0;
        }
    }

    private int importPositionsMock(String accountId, Long userId) {
        try {
            Account account = findOrCreateAccount(accountId, userId);
            // В реальной реализации здесь будет вызов API
            log.info("Импорт позиций для аккаунта {}", accountId);
            return 0; // Заглушка
        } catch (Exception e) {
            log.error("Ошибка импорта позиций: {}", e.getMessage(), e);
            return 0;
        }
    }

    private int importDividendsMock(String accountId, Long userId) {
        try {
            Account account = findOrCreateAccount(accountId, userId);
            // В реальной реализации здесь будет вызов API
            log.info("Импорт дивидендов для аккаунта {}", accountId);
            return 0; // Заглушка
        } catch (Exception e) {
            log.error("Ошибка импорта дивидендов: {}", e.getMessage(), e);
            return 0;
        }
    }

    private Account findOrCreateAccount(String tinkoffAccountId, Long userId) {
        List<Account> accounts = accountService.getAccountsByUser(userId);
        return accounts.stream()
                .filter(a -> tinkoffAccountId.equals(a.getAccountNumber()))
                .findFirst()
                .orElseGet(() -> accountService.createAccount(
                        userId, tinkoffAccountId, "Т-Инвестиции", "INVESTMENT", "RUB"));
    }

    private int estimateOperations(TinkoffSyncMode mode) {
        return switch (mode) {
            case HISTORY -> 250;
            case POSITIONS -> 25;
            case DIVIDENDS -> 40;
            case ALL -> 315;
        };
    }
}
