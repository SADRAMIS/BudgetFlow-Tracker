package com.example.budgetflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "import_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private String source; // "TINKOFF_API", "CSV", "EXCEL"
    
    @Column(nullable = false)
    private LocalDateTime importDate;
    
    @Column(nullable = false)
    private String status; // "SUCCESS", "FAILED", "PARTIAL"
    
    private Integer operationsImported;
    
    private Integer assetsImported;
    
    @Column(length = 1000)
    private String errorMessage;
    
    @Column(length = 2000)
    private String details; // JSON с деталями импорта
}

