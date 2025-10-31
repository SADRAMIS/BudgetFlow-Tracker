package com.example.budgetflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Entity
@Table(name = "trades")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assert_id",nullable = false)
    private Asset asset;
    @Column(nullable = false)
    private String type; // "BUY", "SELL"
    @Column(nullable = false)
    private LocalDate date;
    private Double quantity;
    private Double price;
    private Double fee; // комиссия брокера
}
