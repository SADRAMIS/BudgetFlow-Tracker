package com.example.budgetflow.service;

import com.example.budgetflow.entity.Accrual;
import com.example.budgetflow.repository.AccrualRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AccrualService {
    private final AccrualRepository accrualRepository;

    public List<Accrual> getAccrualsByAsset(Long assetId){
        return accrualRepository.findByAssetId(assetId);
    }

    public List<Accrual> getAccrualsByAssetAndType(Long assetId,String type){
        return accrualRepository.findByAssetIdAndType(assetId,type);
    }

    public List<Accrual> getAccrualsByAssetAndDateRange(Long assetId, LocalDate start,LocalDate end){
        return accrualRepository.findByAssetIdAndDateBetween(assetId,start,end);
    }
}
