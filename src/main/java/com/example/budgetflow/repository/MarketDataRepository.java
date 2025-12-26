package com.example.budgetflow.repository;

import com.example.budgetflow.entity.MarketData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MarketDataRepository extends JpaRepository<MarketData, Long> {
    Optional<MarketData> findFirstByTickerOrderByDateDesc(String ticker);
    
    List<MarketData> findByTickerAndDateBetween(String ticker, LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT md FROM MarketData md WHERE md.ticker = :ticker AND md.date <= :date ORDER BY md.date DESC")
    Optional<MarketData> findLatestByTickerAndDate(@Param("ticker") String ticker, @Param("date") LocalDateTime date);
}

