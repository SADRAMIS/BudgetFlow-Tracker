package com.example.budgetflow.controller;

import com.example.budgetflow.entity.Account;
import com.example.budgetflow.entity.Accrual;
import com.example.budgetflow.entity.Asset;
import com.example.budgetflow.entity.Transaction;
import com.example.budgetflow.service.AccountService;
import com.example.budgetflow.service.AssetService;
import com.example.budgetflow.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
    private final TransactionService transactionService;
    private final AccountService accountService;
    private final AssetService assetService;


    @GetMapping("/balance/{userId}")
    public Double getUserBalance(@PathVariable Long userId){
        return transactionService.getUserBalance(userId);
    }

    @GetMapping("/monthly/{userId}")
    public List<Transaction> getMonthlyTransactions(@PathVariable Long userId,
                                                    @RequestParam int year,
                                                    @RequestParam int month){
        return transactionService.getMonthlyTransactions(userId,year,month);
    }

    @GetMapping("/dividends/{userId}")
    public List<Accrual> getUserDividends(@PathVariable Long userId) {
        // Получаем все аккаунты пользователя
        List<Account> accounts = accountService.getAccountsByUser(userId);
        List<Accrual> allAccruals = new ArrayList<>();
        for (Account account : accounts) {
            List<Asset> assets = assetService.getAssetsByAccount(account.getId());
            for (Asset asset : assets) {
                List<Accrual> accruals = accrualService.getAccrualsByAsset(asset.getId());
                allAccruals.addAll(accruals);
            }
        }
        return allAccruals;
    }

    @GetMapping("/structure/{userId}")
    public Map<String, Double> getPortfolioStructure(@PathVariable Long userId) {
        List<Account> accounts = accountService.getAccountsByUser(userId);
        Map<String, Double> structure = new HashMap<>();
        for (Account account : accounts) {
            List<Asset> assets = assetService.getAssetsByAccount(account.getId());
            for (Asset asset : assets) {
                structure.put(
                        asset.getType(),
                        structure.getOrDefault(asset.getType(), 0.0) + (asset.getQuantity() != null ? asset.getQuantity() : 0.0)
                );
            }
        }
        return structure;
    }

    @GetMapping("/assets/{userId}")
    public Map<String, List<Asset>> getAssetsByCurrency(@PathVariable Long userId) {
        List<Account> accounts = accountService.getAccountsByUser(userId);
        Map<String, List<Asset>> assetsByCurrency = new HashMap<>();
        for (Account account : accounts) {
            List<Asset> assets = assetService.getAssetsByAccount(account.getId());
            for (Asset asset : assets) {
                assetsByCurrency.computeIfAbsent(asset.getCurrency(), k -> new ArrayList<>()).add(asset);
            }
        }
        return assetsByCurrency;
    }



}
