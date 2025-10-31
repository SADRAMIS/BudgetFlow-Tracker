package com.example.budgetflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "accruals")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Accrual {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assert_id",nullable = false)
    private Asset asset;
    @Column(nullable = false)
    private String type; // "DIVIDEND", "COUPON"
    @Column(nullable = false)
    private LocalDate date;
    @Column(nullable = false)
    private Double amount; // сумма начисления
}
