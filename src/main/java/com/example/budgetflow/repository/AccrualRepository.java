package com.example.budgetflow.repository;

import com.example.budgetflow.entity.Accrual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AccrualRepository extends JpaRepository<Accrual,Long> {

    List<Accrual> findByAssetId(Long assetId);
    List<Accrual> findByType(String type);
    List<Accrual> findByDateBetween(LocalDate startDate,LocalDate endDate);
}
