package com.example.budgetflow.repository;

import com.example.budgetflow.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account,Long> {
    List<Account> findByUserId(Long userId);

    Account findByAccountNumber(String accountNumber);
    boolean existsByName(String name);
}
