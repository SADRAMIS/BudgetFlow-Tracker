package com.example.budgetflow.controller;

import com.example.budgetflow.entity.Asset;
import com.example.budgetflow.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {
    private final AssetService assetService;

    @GetMapping("/account/{accountId}")
    public List<Asset> getByAccount(@PathVariable Long accountId){
        return assetService.getAssetsByAccount(accountId);
    }

    @PostMapping
    public ResponseEntity<Asset> create(@RequestBody Asset asset){
        Asset created = assetService.createAsset(
                asset.getAccount().getId(),asset.getName(),asset.getTicker(),
                asset.getType(),asset.getCurrency(),asset.getQuantity()
        );
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public Asset update(@PathVariable Long id,@RequestBody Asset asset){
        return assetService.updateAsset(
                id, asset.getName(), asset.getTicker(), asset.getType(), asset.getCurrency(),asset.getQuantity()
        );
    }
}
