package com.example.budgetflow.repository;

import com.example.budgetflow.entity.PortfolioSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PortfolioSnapshotRepository extends JpaRepository<PortfolioSnapshot, Long> {
    List<PortfolioSnapshot> findByUserIdOrderByDateDesc(Long userId);
    
    Optional<PortfolioSnapshot> findByUserIdAndDate(Long userId, LocalDate date);
    
    @Query("SELECT ps FROM PortfolioSnapshot ps WHERE ps.user.id = :userId AND ps.date >= :startDate ORDER BY ps.date ASC")
    List<PortfolioSnapshot> findByUserIdAndDateAfter(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
}

