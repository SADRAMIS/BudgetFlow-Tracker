package com.example.budgetflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "portfolio_snapshots", indexes = {
    @Index(name = "idx_user_date", columnList = "user_id,date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(precision = 19, scale = 2)
    private BigDecimal totalValue;
    
    @Column(precision = 19, scale = 2)
    private BigDecimal totalCost;
    
    @Column(precision = 19, scale = 4)
    private BigDecimal totalReturn; // в процентах
    
    @Column(precision = 19, scale = 4)
    private BigDecimal sharpeRatio;
    
    private String currency;
}

