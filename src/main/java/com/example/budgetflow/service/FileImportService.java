package com.example.budgetflow.service;

import com.example.budgetflow.entity.*;
import com.example.budgetflow.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileImportService {

    private final UserService userService;
    private final AccountService accountService;
    private final AssetService assetService;
    private final TradeRepository tradeRepository;
    private final AccrualRepository accrualRepository;
    private final ImportLogRepository importLogRepository;

    @Transactional
    public ImportResult importOperationsFile(MultipartFile file, Long userId, String accountNumber) {
        ImportLog logEntry = new ImportLog();
        logEntry.setUser(userService.getUserById(userId));
        logEntry.setSource("CSV");
        logEntry.setImportDate(java.time.LocalDateTime.now());
        logEntry.setStatus("IN_PROGRESS");

        try {
            Account account = findOrCreateAccount(accountNumber, userId);
            List<String> errors = new ArrayList<>();
            int operationsCount = 0;
            int assetsCount = 0;

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), "UTF-8"))) {
                
                String line = reader.readLine(); // пропускаем заголовок
                if (line == null || !line.contains("Дата") && !line.contains("Date")) {
                    throw new IllegalArgumentException("Неверный формат файла. Ожидается CSV с заголовками.");
                }

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                line = reader.readLine();

                while (line != null && !line.trim().isEmpty()) {
                    try {
                        String[] parts = parseCSVLine(line);
                        if (parts.length >= 5) {
                            LocalDate date = LocalDate.parse(parts[0].trim(), formatter);
                            String operation = parts[1].trim();
                            String ticker = parts[2].trim();
                            Double quantity = parseDouble(parts[3]);
                            Double price = parseDouble(parts[4]);
                            Double fee = parts.length > 5 ? parseDouble(parts[5]) : 0.0;

                            if ("Покупка".equals(operation) || "BUY".equalsIgnoreCase(operation) ||
                                "Продажа".equals(operation) || "SELL".equalsIgnoreCase(operation)) {
                                
                                Asset asset = findOrCreateAsset(ticker, account);
                                Trade trade = new Trade();
                                trade.setAsset(asset);
                                trade.setType("Покупка".equals(operation) || "BUY".equalsIgnoreCase(operation) ? "BUY" : "SELL");
                                trade.setDate(date);
                                trade.setQuantity(quantity);
                                trade.setPrice(price);
                                trade.setFee(fee);
                                
                                tradeRepository.save(trade);
                                operationsCount++;
                            }
                        }
                    } catch (Exception e) {
                        errors.add("Строка: " + line + " - " + e.getMessage());
                    }
                    line = reader.readLine();
                }
            }

            logEntry.setStatus(errors.isEmpty() ? "SUCCESS" : "PARTIAL");
            logEntry.setOperationsImported(operationsCount);
            logEntry.setAssetsImported(assetsCount);
            logEntry.setErrorMessage(errors.isEmpty() ? null : String.join("; ", errors));
            logEntry.setDetails(String.format("{\"operations\":%d,\"assets\":%d,\"errors\":%d}", 
                    operationsCount, assetsCount, errors.size()));

            importLogRepository.save(logEntry);
            
            return new ImportResult(true, operationsCount, assetsCount, errors);
        } catch (Exception ex) {
            log.error("Ошибка импорта файла: {}", ex.getMessage(), ex);
            logEntry.setStatus("FAILED");
            logEntry.setErrorMessage(ex.getMessage());
            importLogRepository.save(logEntry);
            return new ImportResult(false, 0, 0, List.of(ex.getMessage()));
        }
    }

    private String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        
        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        return result.toArray(new String[0]);
    }

    private Double parseDouble(String str) {
        if (str == null || str.trim().isEmpty()) return 0.0;
        try {
            return Double.parseDouble(str.replace(",", ".").replace(" ", ""));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private Account findOrCreateAccount(String accountNumber, Long userId) {
        List<Account> accounts = accountService.getAccountsByUser(userId);
        return accounts.stream()
                .filter(a -> accountNumber.equals(a.getAccountNumber()))
                .findFirst()
                .orElseGet(() -> accountService.createAccount(
                        userId, accountNumber, "Импортированный счёт", "INVESTMENT", "RUB"));
    }

    private Asset findOrCreateAsset(String ticker, Account account) {
        List<Asset> assets = assetService.getAssetsByAccount(account.getId());
        return assets.stream()
                .filter(a -> ticker.equals(a.getTicker()))
                .findFirst()
                .orElseGet(() -> assetService.createAsset(
                        account.getId(), ticker, ticker, "stock", "RUB", 0.0));
    }

    public record ImportResult(boolean success, int operationsCount, int assetsCount, List<String> errors) {}
}

