package com.example.budgetflow.repository;

import com.example.budgetflow.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetRepository extends JpaRepository<Asset,Long> {

    List<Asset> findByAccountId(long accountId);
    List<Asset> findByType(String type);
    List<Asset> findByCurrency(String currency);

    Asset findByTicker(String ticker);
}
