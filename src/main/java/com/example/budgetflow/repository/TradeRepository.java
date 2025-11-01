package com.example.budgetflow.repository;

import com.example.budgetflow.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TradeRepository extends JpaRepository<Trade,Long> {
    List<Trade> findByAssetId(Long assetId);
    List<Trade> findByDateBetween(LocalDate startDate, LocalDate endDate);
}
