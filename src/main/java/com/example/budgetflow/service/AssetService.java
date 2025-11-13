package com.example.budgetflow.service;

import com.example.budgetflow.entity.Account;
import com.example.budgetflow.entity.Asset;
import com.example.budgetflow.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AssetService {
    private final AssetRepository assetRepository;
    private final AccountService accountService;

    public List<Asset> getAssetsByAccount(Long accountId){
        return assetRepository.findByAccountId(accountId);
    }

    public Asset getAssetById(Long assetId){
        return assetRepository.findById(assetId)
                .orElseThrow(()-> new IllegalArgumentException("Актив не найден с ID: " + assetId));
    }

    public Asset createAsset(Long accountId,String name,String ticker,String type,String currency,Double quantity){
        Account account = accountService.getAccountById(accountId);
        Asset asset = new Asset();
        asset.setAccount(account);
        asset.setName(name);
        asset.setTicker(ticker);
        asset.setType(type);
        asset.setCurrency(currency);
        asset.setQuantity(quantity);
        return assetRepository.save(asset);
    }

    public Asset updateAsset(Long assetId,String name,String ticker,String type,String currency,Double quantity){
        Asset asset = getAssetById(assetId);
        if (name != null) asset.setName(name);
        if (ticker != null) asset.setTicker(ticker);
        if (type != null) asset.setType(type);
        if (currency != null) asset.setCurrency(currency);
        if (quantity != null) asset.setQuantity(quantity);
        return assetRepository.save(asset);
    }

    public void deleteAccount(Long assetId){
        log.info("Удаление актива с ID: {}", assetId);
        assetRepository.deleteById(assetId);
    }
}
