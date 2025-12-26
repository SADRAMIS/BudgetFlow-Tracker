package com.example.budgetflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "market_data", indexes = {
    @Index(name = "idx_ticker_date", columnList = "ticker,date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarketData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String ticker;
    
    @Column(nullable = false)
    private LocalDateTime date;
    
    @Column(precision = 19, scale = 4)
    private BigDecimal price;
    
    @Column(precision = 19, scale = 4)
    private BigDecimal volume;
    
    private String currency;
    
    private String source; // "TINKOFF", "MANUAL", "IMPORT"
}

