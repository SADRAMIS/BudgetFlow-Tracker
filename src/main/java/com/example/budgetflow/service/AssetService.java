package com.example.budgetflow.service;

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
}
