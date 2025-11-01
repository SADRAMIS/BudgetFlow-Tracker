package com.example.budgetflow.repository;

import com.example.budgetflow.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trade,Long> {
    List<Trade> findByAssetId(Long assetId);
    List<Trade> findByDateBetween(LocalDate startDate, LocalDate endDate);
}
